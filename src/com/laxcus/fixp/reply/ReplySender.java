/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.io.*;
import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 异步数据发送器。 把本地异步数据发送到目标地址。<br>
 * 
 * 提供线程能力的的ReplyClient。
 * 
 * 注意：ReplySender只在收到请求时发送数据包，而不是主动推送！
 * 
 * @author scott.liang
 * @version 1.0 7/22/2018
 * @since laxcus 1.0
 */
public class ReplySender extends ReplyProvider implements Runnable {

	/** 异步发送代理 **/
	private ReplyWorker worker;

	/** 判断成功 **/
	private boolean successful;

	/** 发送等待者，监视工作完成，不论成功或者失败 **/
	private DynamicWaiter waiter;

	/** 文件或者内存数据的下标位置 **/
	private int seek;

	/** 指定文件的下标 **/
	private int fileIndex;

	/** 发送的磁盘文件 **/
	private ArrayList<File> files;

	/** 准备发送的数据 **/
	private byte[] data;

	/** 当前需要投递的子包集 **/
	private ArrayList<Packet> subpackets;

	/** 子包统计数目 **/
	private int subPacketsCount;

	/** 退出标记，发送“退出”数据包后设置。 **/
	private boolean exit;

	/** REPLY WORKER投递的数据包缓存集合 **/
	private ArrayList<Packet> buffers ;

	/** 客户机编号，保证唯一 **/
	private long clientId;

	/** 最后驻留时间 **/
	private long stayTime;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.reply.ReplyWatcher#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		worker = null;
		data = null;
		files = null;
		// 清除包
		subpackets = null;
		buffers = null;
		// 清除等待器
		waiter = null;
	}

	/**
	 * 构造默认的异步数据发送器
	 */
	public ReplySender() {
		super();

		// 生成等待器
		waiter = new DynamicWaiter();

		// 接收的反馈包集合
		buffers = new ArrayList<Packet>(256);

		// 子包集合
		subpackets = new ArrayList<Packet>(512);

		// 文件序列索引
		fileIndex = 0;
		files = new ArrayList<File>();
		// 读取的文件或者内存数据下标位置
		seek = 0;
		// 子包数0
		subPacketsCount = 0;
		// 退出标记
		exit = false;

		// 线程终止标记
		interrupted = false;
		// 线程运行
		running = false;
		// 线程句柄
		thread = null;

		// 产生序列号
		nextSerial();

		// 驻留开始时间
		stayTime = System.currentTimeMillis();
	}

	/**
	 * 分配一个序列号，保证全局唯一！
	 */
	private void nextSerial() {
		clientId = generator.nextSerial();
	}

	/**
	 * 返回子包统计数目
	 * 
	 * @return 子包数目
	 */
	public int getSubPackets() {
		return subPacketsCount;
	}

	/**
	 * 判断处于等待状态
	 * 
	 * @return 返回真或者假
	 */
	public boolean isAwaiting() {
		return waiter.isAwaiting();
	}

	/**
	 * 触发唤醒对象
	 */
	public void done() {
		waiter.done();
	}

	/**
	 * 进入等待状态，直到返回处理结果
	 */
	public void await() {
		waiter.await();
	}

	/**
	 * 设置异步发送代理
	 * 
	 * @param e
	 */
	public void setWorker(ReplyWorker e) {
		worker = e;
	}

	/**
	 * 设置成功标记
	 * 
	 * @param b 成功标记
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断成功
	 * 
	 * @return 成功真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 设置一组文件
	 * 
	 * @param a 文件数组
	 */
	public void setFiles(File[] a) {
		for (int i = 0; a != null && i < a.length; i++) {
			if (a[i] != null) {
				files.add(a[i]);
			}
		}
	}

	/**
	 * 设置文件
	 * 
	 * @param e 文件实例
	 */
	public void setFile(File e) {
		setFiles(new File[] { e });
	}

	/**
	 * 输出全部文件
	 * 
	 * @return 文件数组
	 */
	public File[] getFiles() {
		File[] a = new File[files.size()];
		return files.toArray(a);
	}

	/**
	 * 判断是发送文件
	 * 
	 * @return 返回真或者假
	 */
	public boolean hasFile() {
		return files.size() > 0;
	}

	/**
	 * 设置发送的字节数组
	 * 
	 * @param b
	 */
	public void setData(byte[] b) {
		data = b;
	}

	/**
	 * 输出发送的字节数组
	 * 
	 * @return 字节数组
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * 输出数据长度
	 * 
	 * @return
	 */
	public int getDataLength() {
		return data.length;
	}

	/**
	 * 判断发送数据
	 * 
	 * @return 返回真或者假
	 */
	public boolean hasData() {
		return data != null && data.length > 0;
	}

	/**
	 * 判断已经退出
	 * 
	 * @return 返回真或者假
	 */
	public boolean isExit() {
		return exit;
	}

	/**
	 * 使用异步通信令牌生成HELP包，发送给REPLY SUCKER服务器
	 * @return 成功返回真，否则假
	 */
	protected boolean helo() {
		// 异步通信令牌
		CastToken token = getToken();

		// 生成HELO包
		Packet packet = new Packet(Ask.NOTIFY, Ask.CASTHELO);
		packet.setRemote(token.getListener()); // 目标站点地址
		packet.addMessage(MessageKey.CAST_FLAG, token.getFlag());
		// 发送给服务器
		int count = sendTo(packet, 3);
		return (count > 0);
	}

	/**
	 * 解析数据流量控制
	 * @param packet FIXP包
	 */
	private void doFlowControl(Packet packet) {
		// 传输模式
		Integer iMode = packet.findInteger(MessageKey.SUBPACKET_TRANSFER_MODE);
		if (iMode == null) {
			return;
		}
		// 一次方法调用发送的子包数目
		Integer iUnit = packet.findInteger(MessageKey.SUBPACKET_UNIT);
		if (iUnit == null) {
			return;
		}
		// 单个子包内容尺寸
		Integer iSize = packet.findInteger(MessageKey.SUBPACKET_CONTENT_SIZE);
		if (iSize == null) {
			return;
		}
		// 定义一个新的子包尺寸
		Integer iInterval = packet.findInteger(MessageKey.SUBPACKET_SEND_INTERVAL);
		if (iInterval == null) {
			return;
		}
		// 子包等待超时时间
		Integer iTimeout = packet.findInteger(MessageKey.SUBPACKET_FEEDBACK_TIMEOUT);
		if (iTimeout == null) {
			return;
		}

		// 设置参数
		setMode(iMode.intValue()); // 传输模式
		setSendUnit(iUnit.intValue()); // 单位发送子包数目
		setSubPacketSize(iSize.intValue()); // 单个子包尺寸
		setSendInterval(iInterval.intValue()); // 发送间隔时间：单位：毫秒
		setFeedbackTimeout(iTimeout.intValue()); // 超时，单位：毫秒

		//		// 控制流
		//		Logger.debug(this, "doFlowControl", "%s (%d#%d#%d)",
		//				ReplyTransfer.translateTransferMode(mode), sendUnit,
		//				getSubPacketSize(), sendInterval);
	}

	/**
	 * 发送重复的FIXP UDP包！
	 * @param packet FIXP子包
	 * @param count 发送数目
	 * @return 返回发送包数目
	 */
	private int __sendTo(Packet packet, int count) {
		// 刷新时间
		refreshPacketTime();
		// 发送数据包
		return worker.sendTo(packet, count);
	}

	/**
	 * 发送一批FIXP UDP包
	 * @param remote 目标地址
	 * @param packets 数据包集合
	 * @return 返回发送成功的数目
	 */
	private int __allTo(SocketHost remote, List<Packet> packets) {
		// 刷新时间
		refreshPacketTime();
		// 发送数据包
		byte serverFamily = getToken().getServerFamily();
		return worker.allTo(serverFamily, remote, packets);
	}

	/**
	 * 发送前的延时驻留
	 */
	private void stay() {
		if (sendInterval > 0) {
			long time = System.currentTimeMillis() - stayTime;
			if (time < sendInterval) {
				delay(sendInterval - time);
			}
		}
		// 记录时间
		stayTime = System.currentTimeMillis();
	}

	/**
	 * 发送一组FIXP数据包
	 * @param packets FIXP数据包
	 * @return 返回发送成功的子包数目
	 */
	private int sendTo(Packet packet, int count) {
		int sendCount = 0;
		// 判断是并行模式发送
		if (ReplyTransfer.isParallelTransfer(mode)) {
			sendCount = __sendTo(packet, count);
		}
		// 以串行模式发送
		else {
			SocketHost host = packet.getRemote();
			// 加串行锁，
			FlowLock.getInstance().lock(host, clientId);
			// 发送包
			sendCount = __sendTo(packet, count);
			// 串行解锁
			FlowLock.getInstance().unlock(host, clientId);
		}
		// 返回结果
		return sendCount;
	}

	/**
	 * 发送一个数据包
	 * @param packet FIXP子包
	 * @return 成功返回真，否则假
	 */
	private boolean sendTo(Packet packet) {
		int count = sendTo(packet, 1);
		return (count > 0);
	}

	/**
	 * 发送一批FIXP数据包
	 * @param packets FIXP数据包
	 * @return 返回发送成功的子包数目
	 */
	private int allTo(List<Packet> packets) {
		int size = packets.size();

		int count = 0;
		int index = 0;
		// 循环发送
		while (index < size) {
			// 无论并行/串行，在发送前都执行延时驻留
			stay();

			// 选择一个最小单位
			int left = (size - index > sendUnit ? sendUnit : size - index);
			List<Packet> subs =	packets.subList(index, index + left);

			// 目标地址
			SocketHost host = subs.get(0).getRemote();

			// 判断是并行模式发送
			if (ReplyTransfer.isParallelTransfer(mode)) {
				int ret = __allTo(host, subs);
				count += ret;
			}
			// 以串行模式发送
			else {
				// 加串行锁，
				FlowLock.getInstance().lock(host, clientId);
				int ret = __allTo(host, subs);
				count += ret;
				// 串行解锁
				FlowLock.getInstance().unlock(host, clientId);
			}

			// 移动索引位置
			index += left;
		}
		return count;
	}

	/**
	 * 结束会话，双方关闭SOCKET
	 * 
	 * @return 发送成功且收到反馈，返回真，否则假
	 */
	private void exit() {
		Packet packet = new Packet(Ask.NOTIFY, Ask.CASTEXIT);
		packet.addMessage(MessageKey.CAST_CODE, getCastCode());
		packet.setRemote(getToken().getListener());

		// 把发送的数据包保存在缓存中，等待REPLY WORKER发送出去
		int count = sendTo(packet, 3);

		// 判断发送成功
		if (count > 0) {
			exit = true;
		}
	}

	/**
	 * 分发单个文件
	 * 
	 * @param file 文件名
	 * @return 出错返回真，否则假
	 */
	private boolean transferFile(File file) {
		// Logger.debug(this, "transferFile", "这个文件是：%s", file);

		// 截取一段长度的数据
		int len = (int) Laxkit.limit(seek, file.length(), getPacketSize());

		// 切割数据，分成多个子包
		List<Packet> subs = null;
		try {
			byte[] b = new byte[len];
			FileInputStream in = new FileInputStream(file);
			// 移到到指定的文件下标，位置必须决定保证一致！
			long pos = in.skip(seek);
			if (pos != seek) {
				String s = String.format("pos:%d , seek:%d", pos, seek);
				throw new IOException(s);
			}
			// 读取一段数据
			in.read(b);
			// 关闭文件
			in.close();
			// 按照规定的子包尺寸，生成FIXP子包集合
			subs = split(b, 0, b.length, getSubPacketSize());
		} catch (SecureException e) {
			Logger.error(e);
		} catch (IOException e) {
			Logger.error(e);
		}
		// 出错
		if (subs == null) {
			Logger.error(this, "transferFile", "split error!");
			return true;
		}

		// 清除旧数据，保存新数据
		subpackets.clear();
		subpackets.addAll(subs);
		// 移到下标
		seek += len;

		// 发送全部子包
		allTo(subpackets);

		// 统计子包数目
		subPacketsCount += subpackets.size();

		// 统计发送字节
		addSendFlowSize(len);

		// 返回假
		return false;
	}

	/**
	 * 判断文件发送完成
	 * 
	 * @return 返回真或者假
	 */
	private boolean isFileFinished() {
		return files.size() > 0 && fileIndex >= files.size();
	}

	/**
	 * 检查和分发文件
	 * 
	 * @return 之前发送完成返回真，否则假
	 */
	private boolean transferFiles() {
		File file = null;
		while (true) {
			// 文件全部传完，返回真，触发“CASE EXIT”通知给接收端
			if (fileIndex >= files.size()) {
				return true;
			}
			// 取出下标处的文件
			file = files.get(fileIndex);
			// 是空文件，或者文件已经写完，移到下一个文件
			if (file.length() == 0 || seek >= file.length()) {
				// 移到下个文件，文件指针归0
				fileIndex++;
				seek = 0;
				continue;
			} else {
				break;
			}
		}

		// 从这个文件中取出数据
		return transferFile(file);
	}

	/**
	 * 判断内存数据发送完成
	 * 
	 * @return 返回真或者假
	 */
	public boolean isMemoryFinished() {
		return data != null && seek >= data.length;
	}

	/**
	 * 分发内存数据
	 * 
	 * @return 之前发送完成返回真，否则假
	 */
	private boolean transferMemory() {
		// 如果达到最大长度，返回真，发送CAST EXIT命令
		if (seek >= data.length) {
			return true;
		}

		// 截取一段长度的数据
		int len = Laxkit.limit(seek, data.length, getPacketSize());

		// 切割数据，分成多个子包
		List<Packet> subs = null;
		try {
			// 按照规定的子包尺寸，生成FIXP子包集
			subs = split(data, seek, len, getSubPacketSize());
		} catch (SecureException e) {
			Logger.error(e);
		}
		// 出错
		if (subs == null) {
			Logger.error(this, "doData", "split error!");
			return true;
		}

		// 清除旧数据，保存新数据
		subpackets.clear();
		subpackets.addAll(subs);
		// 移到下标
		seek += len;

		// 发送全部子包
		allTo(subpackets);
		// 子包数目
		subPacketsCount += subpackets.size();

		// 统计发送的字节
		addSendFlowSize(len);

		return false;
	}

	/**
	 * 启动数据传输
	 * 
	 * @return 成功返回真，否则假
	 */
	private void init() {
		boolean quit = false;
		if (files.size() > 0) {
			quit = transferFiles();
		} else if (data != null) {
			quit = transferMemory();
		} else {
			quit = true;
		}
		// 以上没有数据，触发退出
		if (quit) {
			exit();
		}
	}

	/**
	 * 在收到“CAST_OKAY”后，向来源地址返回“CAST_OKAY_REPLY”，形成三段结果。<br>
	 * 三段： ReplyReceiver(CAST_OKAY) -> ReplyClient(CAST_OKAY_REPLY) -> ReplyReceiver <br>
	 * 
	 * 确认保证发出！
	 * 
	 * @param sendId 包编号
	 * @param count 发送次数
	 */
	private void confirm(int sendId, int count) {
		if (count < 1) {
			count = 1;
		}

		Packet packet = new Packet(Answer.CAST_OKAY_REPLY);
		packet.setRemote(getToken().getListener());
		packet.addMessage(MessageKey.CAST_CODE, getCastCode());
		packet.addMessage(MessageKey.PACKET_IDENTIFY, sendId);
		packet.addMessage(MessageKey.SPEAK, "From ReplySender!");

		while (true) {
			// 向目标地址发向3个包
			//			int all = sendTo(packet, count);

			// 向目标地址发送N个包
			int all = sendTo(packet, count);
			boolean success = (all > 0);
			if (success) {
				break;
			} else {
				delay(1000);
			}
		}
	}

	/**
	 * 子包递进
	 */
	private void nextPacket() {
		// 清除前次子包数据
		subpackets.clear();
		// 包序列号加1，移动到下一组
		packetId += 1;
	}

	/**
	 * 收到消息，发送下一组!
	 * @param packet 数据包
	 */
	private void next(Packet packet) {
		// 解析流量控制
		doFlowControl(packet);

		// 包编号一致才处理!
		Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
		if (id == null) {
			Logger.error(this, "next", "cannot be find message key: \'PACKET_IDENTITY\'!!!");
			return;
		}

		int fromId = id.intValue();
		// 1. 包编号一致，发送反馈，移到下一组包
		if (packetId == fromId) {
			//			Logger.debug(this, "next", "反馈匹配，下一组包！来自 %s , remote %d = local %d", packet.getRemote(), fromId, packetId);

			confirm(fromId, 2);
			// 移到包位置，继续发送下一组包!
			nextPacket();
		}
		// 不一致时，忽略！
		else {
//						Logger.warning(this, "next", "包编号不一致！%d != %d", fromId, packetId);
			return;
		}

		// 已经退出，再次发一次
		if (isExit()) {
			exit();
			return;
		}

		// 检查已经发送完成
		if (isFileFinished() || isMemoryFinished()) {
			exit();
			return;
		}

		// 启动下一阶段的发送
		boolean quit = false; // 退出标记
		if (files.size() > 0) {
			quit = transferFiles();
		} else if (data != null) {
			quit = transferMemory();
		} else {
			quit = true;
		}
		// 到达最后，触发退出！
		if (quit) {
			exit();
		}
	}

	/**
	 * ReplyReceiver进入锁定状态后，发送包，本处是接收处理
	 * @param packet 数据包
	 */
	private void lock_next(Packet packet) {
		// 解析流量控制
		doFlowControl(packet);

		// 包编号一致才处理!
		Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
		if (id == null) {
			Logger.error(this, "lock_next", "cannot be find message key: \'PACKET_IDENTITY\'!!!");
			return;
		}

		int fromId = id.intValue();
		// 1. "next"方法没有收到处理。包编号一致，发送反馈，移到下一组包
		if (packetId == fromId) {
			//			Logger.debug(this, "lock_next", "反馈匹配，下一组包！来自 %s, remote %d = local %d", 
			//					packet.getRemote(), fromId, packetId);

			confirm(fromId, 2);
			// 移到包位置，继续发送下一组包!
			nextPacket();
		}
		// 2. "next"方法收到CAST_OKAY且返回，但是ReplyReceiver没有收到"CAST_OKAY_REPLY"，超时后发送“LOCK_CAST_OKAY”
		else if (packetId == fromId + 1) {
			//			Logger.warning(this, "lock_next", "冗余包！来自 %s, remote %d + 1 = local %d", packet.getRemote(), fromId, packetId);

			confirm(fromId, 2);// 再次确认
			// 以下两种情况！
			if (isExit()) {
				exit(); // 判断退出
			} 
			return;
		}
		// 其它情况，忽略!
		else {
//			Logger.error(this, "lock_next", "包编号不一致！来自 %s, remote %d != local %d", packet.getRemote(), fromId, packetId);
			return;
		}

		// 已经退出，再次发一次
		if (isExit()) {
			exit();
			return;
		}

		// 检查已经发送完成
		if (isFileFinished() || isMemoryFinished()) {
			exit();
			return;
		}

		// 启动下一阶段的发送
		boolean quit = false; // 退出标记
		if (files.size() > 0) {
			quit = transferFiles();
		} else if (data != null) {
			quit = transferMemory();
		} else {
			quit = true;
		}
		// 到达最后，触发退出！
		if (quit) {
			exit();
		}
	}

	/**
	 * 重新发送数据包
	 * 
	 * @param packet FIXP子包
	 * @return 成功返回真，否则假
	 */
	private boolean retry(Packet packet) {
		// 解析流量控制
		doFlowControl(packet);

		// 找到编号
		Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
		// 以下条件忽略
		if (id == null || id.intValue() != packetId) {
			return false;
		}

		// 取子包编号
		List<Integer> serials = getSerials(packet, subpackets.size());

		int count = 0;
		// 子包序号是从0开始，逐一取出，发送给请求端
		for (int serial : serials) {
			Packet sub = subpackets.get(serial);
			if (sub != null) {
				boolean b = sendTo(sub);
				if(b) count++;
			}
		}

		// 刷新重试时间
		boolean success = (count > 0);
		if (success) {
			refreshRetryTime();
		}
		return success;
	}

	/**
	 * 发送全部子包
	 * @param packet 请求包
	 * @return 发送成功返回真，否则假
	 */
	private boolean refire(Packet packet) {
		// 解析流量控制
		doFlowControl(packet);

		// 找到包编号
		Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
		// 如果没有定义编号，或者编号不一致时，忽略它！（这是冗余问题，不属于错误）
		if (id == null || id.intValue() != packetId) {
			return false;
		}

		// 发送全部子包
		int count = allTo(subpackets);
		// 判断发送成功
		boolean success = (count > 0);
		// 刷新重试时间
		if (success) {
			refreshRetryTime();
		}
		return success;
	}

	/**
	 * 保存一个反馈回来的数据包
	 * 
	 * @param packet FIXP子包
	 * @return 保存成功返回真，否则假
	 */
	protected boolean add(Packet packet) {
		boolean success = false;
		// 锁定保存
		super.lockSingle();
		try {
			success = buffers.add(packet);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 唤醒线程
		if (success) {
			wakeup();
		}
		return success;
	}

	/**
	 * 弹出一个接收到的数据包
	 * 
	 * @return 返回FIXP包实例，没有返回空指针
	 */
	private Packet popup() {
		super.lockSingle();
		try {
			if (buffers.size() > 0) {
				return buffers.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 检查和处理FIXP包
	 * 
	 * @param packet FIXP数据包
	 */
	private void subprocess(Packet packet) {
		Mark mark = packet.getMark();

		// 判断两种情况：CAST_OKAY, LOCK_CAST_OKAY
		if (Answer.isCastOkay(mark)) { 
			next(packet); // 本次发送成功，下一个包
		} else if(Answer.isLockCastOkay(mark)) {
			lock_next(packet); // 锁定中超时再次确认
		}
		// 重传
		else if (Assert.isCastRefire(mark)) {
			Logger.warning(this, "subprocess", "re-post all sub packets");
			refire(packet); // 发送一次完全的FIXP数据包
		} else if (Assert.isRetrySubPacket(mark)) {
			Logger.warning(this, "subprocess", "re-post sub packet");
			retry(packet);
		} else if (Answer.isCastExitOkay(mark)) {
			// 处理结果
			setSuccessful(true);
			// 返回
			setInterrupted(true);
		} 
//		else {
//			Logger.error(this, "subprocess", "cannot be translate！%s", mark);
//		}
	}

	/**
	 * 处理一组FIXP子包
	 * 
	 * @return 返回处理的子包数目
	 */
	private int process() {
		int size = buffers.size();
		for (int i = 0; i < size; i++) {
			Packet packet = popup();
			if (packet != null) {
				subprocess(packet);
			}
		}
		return size;
	}

	// 以下是线程操作

	/** 线程终止标记 **/
	private boolean interrupted;

	/** 线程运行状态 **/
	private volatile boolean running;

	/** 线程句柄 **/
	private Thread thread;

	/**
	 * 判断线程被要求中断。中断标记为“真”时，即是要求中断
	 * 
	 * @return 返回真或者假
	 */
	protected boolean isInterrupted() {
		return interrupted;
	}

	/**
	 * 设置中断标记
	 * @param b 中断标记
	 */
	private void setInterrupted(boolean b) {
		interrupted = b;
		// 唤醒等待的线程
		if (b) {
			wakeup();
		}
	}

	/**
	 * 判断线程处于运行状态
	 * 
	 * @return 返回真或者假
	 */
	protected boolean isRunning() {
		return running && thread != null;
	}

	/**
	 * 判断线程处于停止状态
	 * 
	 * @return 返回真或者假
	 */
	protected boolean isStopped() {
		return !isRunning();
	}

	/**
	 * 启动线程
	 * 
	 * @param priority 线程优化级，见Thread中的定义
	 * @return 成功返回“真”，失败“假”。
	 */
	protected boolean start(int priority) {
		// 检测线程
		synchronized (this) {
			if (thread != null) {
				return false;
			}
		}
		// 启动线程
		thread = new Thread(this);
		thread.setPriority(priority);
		thread.start();

		return true;
	}

	/**
	 * 使用线程较小优先级启动线程
	 * 
	 * @return 成功返回“真”，失败“假”。
	 */
	protected boolean start() {
		return start(Thread.NORM_PRIORITY);
	}

	/**
	 * 停止线程运行
	 */
	protected void stop() {
		if (interrupted) {
			return;
		}
		setInterrupted(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;
		init();

		// 判断线程运行中
		while (!isInterrupted()) {
			// 处理子包和检查
			int process = process();
			int count = check();
			// 没有处理，延时...
			if (process == 0 && count == 0) {
				delay(1000);
			}
		}

		// 释放自己
		worker.removeSender(replyFlag);
		// 唤醒外部等待
		done();

		running = false;
		thread = null;
	}

	/**
	 * 检查超时
	 * 
	 * @return 发生处理操作返回1，否则0
	 */
	private int check() {
		boolean success = checkDisabledTimeout();
		// 返回1或者0
		return (success ? 1 : 0);
	}

	/**
	 * 达到失效时间
	 * 
	 * @return 返回真或者假
	 */
	private boolean checkDisabledTimeout() {
		// 达到失效时间，删除它！
		boolean happen = isPacketTimeout(ReplyWorker.getDisableTimeout());
		if (happen) {
			Logger.error(this, "checkDisabledTimeout", "Delete disabled! Cast code: %s", getCastCode());
			setInterrupted(true);
		}
		return happen;
	}

}