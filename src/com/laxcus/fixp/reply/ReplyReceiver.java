/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 异步数据接收器。
 * 把数据写入异步调用器的缓存或者硬盘。
 * 
 * @author scott.liang
 * @version 1.0 7/21/2018
 * @since laxcus 1.0
 */
public class ReplyReceiver extends ReplyWatcher implements Runnable {

	/** 固定要求一个请求包包含50个子包编号 **/
	final static int RETRY_SERIALS = 50;

	/** 异步数据写入器 **/
	private CastWriter writer;

	/** 辅助器 **/
	private ReplyHelper helper;

	/** REPLY HELPER投递的数据包集合 **/
	private ArrayList<Packet> packets;

	/** 子包集合 **/
	private PacketBasket basket;

	/** 写入数据下标，从0开始 **/
	private long seek = 0L;

	/** 锁定中...当调用"next"方法发送CAST_OKAY且成功时，此值为TRUE。在收到ReplyClient.CAST_OKAY_REPLY后，此值为假。
	 * 在这个过程中，不处理其它子包。 check方法判断locking=true且超时，调用next方法再请求 **/
	private boolean lock;
	
	/** 锁定时间 **/
	private long lockTime;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.reply.ReplyWatcher#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		writer = null;
		helper = null;
		basket = null;
		if (packets != null) {
			packets.clear();
			packets = null;
		}
	}

	/**
	 * 构造默认的异步数据接收器
	 */
	private ReplyReceiver() {
		super();
		seek = 0L;
		setLock(false);
		refreshLockTime();
		basket = new PacketBasket();
		packets = new ArrayList<Packet>(256);
	}

	/**
	 * 构造快速FIXP数据包接收器，指定写入器和异步通信令牌
	 * @param writer 异步数据写入器
	 * @param token 异步通信令牌
	 */
	public ReplyReceiver(CastWriter writer, CastToken token) {
		this();
		setWriter(writer);
		setToken(token);
	}

	/**
	 * 设置锁定
	 * @param b 真或者假
	 */
	private void setLock(boolean b) {
		lock = b;
//		Logger.debug(this, "setLock", "%s !", (lock ? "锁定" : "解锁"));
	}

	/**
	 * 判断锁定
	 * @return 真或者假
	 */
	private boolean isLock() {
		return lock;
	}

	/**
	 * 刷新锁定时间
	 */
	private void refreshLockTime() {
		lockTime = System.currentTimeMillis();
	}

	/**
	 * 判断锁定超时
	 * @param ms 毫秒
	 * @return 返回真或者假
	 */
	private boolean isLockTimeout(long ms) {
		return System.currentTimeMillis() - lockTime >= ms;
	}

	/**
	 * 设置异步数据写入器，不允许空指针
	 * @param e 异步数据写入器
	 */
	public void setWriter(CastWriter e) {
		Laxkit.nullabled(e);
		writer = e;
	}

	/**
	 * 返回异步数据写入器
	 * @return 异步数据写入器实例
	 */
	public CastWriter getWriter(){
		return writer;
	}

	/**
	 * 设置异步数据辅助器
	 * @param e 异步数据辅助器
	 */
	public void setHelper(ReplyHelper e) {
		Laxkit.nullabled(e);
		helper = e;
	}

	/**
	 * 返回当前接收数据下标位置
	 * @return 数据下标
	 */
	public long getSeek() {
		return seek;
	}

	/**
	 * 调用异步缓存接口，把数据写入本地（内存或者硬盘）。
	 * @param b 字节数组
	 */
	private void writeBuffer(byte[] b) {
		long pos = writer.push(seek, b, 0, b.length);
		if (seek + b.length == pos) {
			seek = pos;
		}
		// 统计接收字节
		addReceiveFlowSize(b.length);
	}

	/**
	 * 连续反馈N个处理包给发送端
	 * @param packet FIXP数据包
	 * @param count 发送次数
	 * @return 返回成功发送的次数
	 */
	private int replyTo(Packet packet, int count) {
		return helper.replyTo(packet, count);
	}

	/**
	 * 向包中增加流量控制信息
	 * @param host
	 * @param packet
	 */
	private void doFlowControl(SocketHost host, Packet packet) {
		FlowSketch sketch = FlowMonitor.getInstance().findSketch(host.getAddress(), replyFlag.getCode());
		if (sketch != null) {
			sketch.influx(packet);
		}
	}

	/**
	 * 确认，递增1
	 * @param packet
	 */
	private void confirm(Packet packet) {
		// 包编号
		Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
		// 没有包编号时...
		if (id == null) {
			Logger.error(this, "confirm", "cannot be find 'PACKET_IDENTITY'");
			return;
		}
		
		int remoteId = id.intValue();
		// 判断编号匹配，退出
		boolean success = (remoteId == packetId);
		
//		Logger.note(this, "confirm", success, "在锁定中，来自 %s, remote %d -> local %d", packet.getRemote(),
//				remoteId, packetId);

		if (success) {
			// 包编号递增1
			packetId += 1;
			// 解除锚定
			setLock(false);
		}
	}

	/**
	 * 接收完成后，通知ReplyClient，本次投递完成，请求下一组FIXP子包。<br>
	 * 因为乱序处理和网络流量大的问题，存在网络丢包现在，本处发送2次。ReplyClient会自动过滤多余的包。
	 * 
	 * @param from 目标地址
	 * @param sendId 当前包编号（不是子包编号！！！）
	 * @param count 发送次数
	 */
	private boolean next(SocketHost from, int sendId, int count) {
		Packet packet = new Packet(Answer.CAST_OKAY);
		packet.setRemote(from);
		packet.addMessage(MessageKey.PACKET_IDENTIFY, sendId);
		packet.addMessage(MessageKey.CAST_CODE, replyFlag.getCode());
		// 增加流控制参数
		doFlowControl(from, packet);

		// 为了防止通信过程中数据包丢失，发送2个
		int ret = replyTo(packet, count);
		return (ret > 0);
	}

	/**
	 * 在锁定状态时，通知ReplyClient，本次投递完成，请求下一组FIXP子包。<br>
	 * 因为乱序处理和网络流量大的问题，存在网络丢包现在，本处发送2次。ReplyClient会自动过滤多余的包。
	 * 
	 * @param from 目标地址
	 * @param sendId 当前包编号（不是子包编号！！！）
	 * @param count 发送次数
	 */
	private boolean lock_next(SocketHost from, int sendId, int count) {
		Packet packet = new Packet(Answer.LOCK_CAST_OKAY);
		packet.setRemote(from);
		packet.addMessage(MessageKey.PACKET_IDENTIFY, sendId);
		packet.addMessage(MessageKey.CAST_CODE, replyFlag.getCode());
		// 增加流控制参数
		doFlowControl(from, packet);

		// 为了防止通信过程中数据包丢失，发送2个
		int ret = replyTo(packet, count);
		return (ret > 0);
	}

	/**
	 * 通知ReplyClient，重新发送一次整个包，请求当前包编号
	 * 
	 * @param from 来源地址
	 * @param sendId 当前FIXP包编号（不是FIXP子包编号啊！！！）
	 */
	private void refire(SocketHost from, int sendId) {
		Packet packet = new Packet(Ask.NOTIFY, Ask.CASTREFIRE);
		packet.setRemote(from);
		packet.addMessage(MessageKey.PACKET_IDENTIFY, sendId);
		packet.addMessage(MessageKey.CAST_CODE, replyFlag.getCode());
		// 增加流控制参数
		doFlowControl(from, packet);

		// 为了防止通信过程中数据包丢失，发送3个
		replyTo(packet, 1);
	}

	/**
	 * 通知REPLY SENDER，收到通知，现在退出！
	 * @param from 来源地址
	 */
	private void exit(SocketHost from) {
		// 生成数据包
		Packet reply = new Packet(Answer.CASTEXIT_OKAY);
		reply.setRemote(from);
		reply.addMessage(MessageKey.CAST_CODE, replyFlag.getCode());
		// 投递3个包
		replyTo(reply, 3);

		// 删除流量记录
		FlowMonitor.getInstance().exit(from.getAddress(), replyFlag.getCode());
	}

	/**
	 * 解密FIXP UDP包的数据域
	 * @param packet 
	 * @return 成功返回真，否则假
	 */
	private boolean decrypt(Packet packet) {
		byte[] b = packet.getData();
		// 生成签名
		long each = Laxkit.doEach(b);
		// 签名比较！
		Long value = packet.findLong(MessageKey.EACH_KEY);
		if (value == null) {
			return false;
		} else if (value.longValue() != each) {
			Logger.error(this, "decrypt", "each key error! packet size:%d, real:%x - local check:%x",
					b.length, each, value.longValue());
			return false;
		}

		// 没有密钥，忽略它
		if (!hasCipher()) {
			return true;
		}

		// 密钥解密数据
		try {
			b = getCipher().decrypt(b, 0, b.length);
			packet.setData(b);
			return true;
		} catch (SecureException e) {
			Logger.error(e, "packet mark: %s, data size:%d", packet.getMark(), b.length);
		}
		return false;
	}

	/**
	 * 保存一个投递过来的数据包
	 * @param packet FIXP子包
	 * @return 保存成功返回真，否则假
	 */
	protected boolean add(Packet packet) {
		boolean success = false;
		// 锁定保存
		super.lockSingle();
		try {
			success = packets.add(packet);
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
	 * @return 返回FIXP包实例，没有返回空指针
	 */
	private Packet popup() {
		// 锁定
		super.lockSingle();
		try {
			if (packets.size() > 0) {
				return packets.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * 处理和导入一个FIXP子包数据到内存或者硬盘
	 * @param packet FIXP数据包
	 */
	private boolean influx(Packet packet) {
		// 拿出当前包编号
		Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
		// 编号不匹配，不是一个批次，忽略它
		if (id == null) {
			Logger.error(this, "influx", "cannot be find \'PACKET_IDENTITY\'");
			return false;
		}
		
		int remoteId = id.intValue();
		if (remoteId != packetId) {
//			Logger.error(this, "influx", "包编号不一致！%d != %d, 通信码：%s", remoteId, packetId, getCastCode());
			return false;
		}
		
		// 解密数据
		boolean success = decrypt(packet);
		if (!success) {
			Logger.error(this, "influx", "cannot decrypt! packet id:%d, subpacket id:%d",
					packetId, packet.findInteger(MessageKey.SUBPACKET_SERIAL));

			// 校验失败，数据传输出现错误。要求发送端重发这个FIXP数据块
			Integer serial = packet.findInteger(MessageKey.SUBPACKET_SERIAL);
			if (serial != null) {
				sendRetryPacket(serial);
			}

			return false;
		}

		// 保存FIXP UDP子包
		success = basket.add(packet);
		if (!success) {
//			Logger.error(this, "influx", "保存子包不成功!");
			return false;
		}

		// 判断全部接收完毕，数据解码，写入本地异步缓存或者硬盘，清空记录
		if (basket.isFull()) {
//			int elements = basket.size();
			
			// 输出全部数据
			byte[] b = basket.flush();
			// 写入本地缓存
			writeBuffer(b);

			// 清除记录
			basket.clear();

			// 发送"CAST_OKAY"给ReplyClient，ReplyClient返回“CAST_OKAY_REPLY”判断一致后，packetId+1
			success = next(packet.getRemote(), packetId, 1);
//			success = lock_next(packet.getRemote(), packetId, 1);
			// 发包成功，锁定，更新锁定时间
			if (success) {
				setLock(true);
				refreshLockTime();
			}

//			Logger.debug(this, "influx", "包填满！成员数 %d, 来自 %s, remote %d -> local %d, %s",
//					elements, packet.getRemote(), remoteId, packetId, (success ? "锁定" : "不锁定"));
			
			//			// 自增1，定位到下个阶段
			//			packetId += 1;
			//			// 发送本阶段包编号给ReplyClient，通知它发送下一阶段的数据包。
			//			next(packet.getRemote(), packetId - 1);

			//			Logger.debug(this, "influx", "本段接收完成，下一段：%d", packetId);
		}

		// 更新最后的FIXP包处理时间
		refreshPacketTime();
		return true;
	}

	/**
	 * 处理一组FIXP子包
	 * @return 有处理返回真，否则假
	 * @return 返回处理的子包数目
	 */
	private int process() {
		int size = packets.size();
		for (int i = 0; i < size; i++) {
			Packet packet = popup();
			if (packet != null) {
				distribute(packet);
			}
		}
		return size;
	}

	/**
	 * 分发包
	 * @param packet
	 */
	private void distribute(Packet packet) {
		Mark mark = packet.getMark();

		// 锁定中只能处理CAST_OKAY_REPLY包，其它包忽略!
		if (isLock()) {
			if (Answer.isCastOkayReply(mark)) {
				confirm(packet);
			}
//			else {
//				if (Assert.isCastExit(mark)) {
//					Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
//					Logger.warning(this, "distribute", "锁定中忽略CAST-EXIT! 来自 %s, 来源编号 %d, 本地编号 %d",
//							packet.getRemote() , (id == null ? -1 : id.intValue()), packetId);
//				} else if (Assert.isCast(mark)) {
//					Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
//					Logger.warning(this, "distribute", "锁定中忽略CAST! 来自 %s, 来源编号 %d, 本地编号 %d",
//							packet.getRemote() , (id == null ? -1 : id.intValue()), packetId);
//				} else {
//					Logger.error(this, "distribute", "锁定中，不能解释包！%s", mark);
//				}
//			}
			return;
		}

		if (Assert.isCastExit(mark)) {
			// 取出异步通信码
			CastCode code = readCode(packet);
			// 判断收到全部包！
			boolean success = (Laxkit.compareTo(replyFlag.getCode(), code) == 0 && basket.isEmpty());
			if (success) {
				// 收到包，反馈退出！
				exit(packet.getRemote());
				// 设置退出标识，线程中通知ReplyHelper删除自己
				setInterrupted(true);

				//				Logger.debug(this, "push", "正常退出! %s", code);
			} else {
				Logger.error(this, "distribute", "Refuse exit! Cast code: %s - %s, packetid:%d, basket subpacket count:%d , size:%d",
						replyFlag.getCode(), code,  packetId,
						basket.count(), basket.size());
			}

			//			Logger.debug(this, "push", success, "退出 %s", code);

		} 
		// 处理数据内容
		else if (Assert.isCast(mark)) {
			influx(packet); // 保存实例包
		} 
		// 非锁定中中的CAST-OKAY-REPLY包，这时忽略不处理
		else if(Answer.isCastOkayReply(mark)) {
//			Logger.warning(this, "distribute", "冗余CAST_OKAY_REPLY，来自: %s", packet.getRemote());
		}
		// 其它可能的包
		else {
			Logger.error(this, "distribute", "Cannot be translate！%s", mark);
		}
	}


	/**
	 * ReplyHelper调用，要求重新发送子包。<br><br>
	 * 两种情况：<br>
	 * 1. 完成没有收到子包，发送完整请求。<br>
	 * 2. 收到部分子包，发送未收到的子包编号。<br>
	 */
	private void retry() {
		// 来源地址
		SocketHost remote = replyFlag.getRemote();

		// 收集没有收到的子包编号
		List<Integer> serials = basket.getMissSerials();
		// 请求一组完整的子包，或者请求未发送的子包。
		if (serials.isEmpty()) {
			FlowSketch sketch = FlowMonitor.getInstance().findSketch(remote.getAddress(), replyFlag.getCode());
			if (sketch != null) {
				FlowMonitor.getInstance().lose(remote.getAddress(), replyFlag.getCode(), sketch.getSendUnit());
			}

//			// 如果包编号是维持在1，没有收到任何子包
//			int sendId = (packetId == 1 ? 1 : packetId - 1);
//			// 要求发送端发送一次完全的数据子包
//			refire(remote, sendId);
//
//			Logger.debug(this, "retry", "包编号：%d，重发全组请求包！", sendId);
			
			// 要求发送端发送一次完全的数据子包
			refire(remote, packetId);
			
//			Logger.error(this, "retry", "包编号：%d，重发全组请求包到：%s", packetId, remote);
		} else {
			// 增加丢包的统计
			FlowMonitor.getInstance().lose(remote.getAddress(), replyFlag.getCode(), serials.size());

			int end = serials.size();
			for (int begin = 0; begin < end;) {
				// 截取一段子包序号
				int size = Laxkit.limit(begin, end, ReplyReceiver.RETRY_SERIALS);
				List<Integer> subs = serials.subList(begin, begin + size);
				// 移到下标
				begin += size;
				// 发送重试包
				sendRetryPackets(subs);
			}

//			Logger.error(this, "retry", "包编号：%d, 包总数：%d, 投递重试包数目：%d 到：%s",
//					packetId, basket.count(), serials.size(), remote);
		}

		// 刷新重试时间
		refreshRetryTime();
	}

	/**
	 * 发送一批重试包
	 * @param serials 序列编号
	 */
	private void sendRetryPackets(List<Integer> serials) {
		SocketHost remote = replyFlag.getRemote();
		// 生成请求包
		Packet packet = new Packet(Ask.NOTIFY, Ask.RETRY_SUBPACKET);
		packet.setRemote(remote); // 目标地址
		packet.addMessage(MessageKey.PACKET_IDENTIFY, packetId);
		packet.addMessage(MessageKey.CAST_CODE, replyFlag.getCode());
		for (int serial : serials) {
			packet.addMessage(MessageKey.SUBPACKET_SERIAL, serial);
		}
		// 增加流控制参数
		doFlowControl(remote, packet);

		// 反馈给请求端，连续发送3次。这种情况下网络存在堵塞，导致丢包故障，发送3次是保证有包抵达目标。
		replyTo(packet, 1);

//		Logger.debug(this, "sendRetryPackets", "通知：%s 重传！", packet.getRemote() );
	}

	/**
	 * 发送一个重试包
	 * @param serial
	 */
	private void sendRetryPacket(int serial) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(serial);
		sendRetryPackets(a);
	}

	///////////
	// 以下是线程操作

	/** 线程终止标记 **/
	private boolean interrupted;

	/** 线程运行状态 **/
	private volatile boolean running;

	/** 线程句柄 **/
	private Thread thread;

	/**
	 * 判断线程被要求中断。中断标记为“真”时，即是要求中断
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
		// 唤醒线程
		if (b) {
			wakeup();
		}
	}
	
	/**
	 * 设置运行状态
	 * @param b 真或者假
	 */
	private void setRunning(boolean b) {
		running = b;
	}

	/**
	 * 判断线程处于运行状态
	 * @return 返回真或者假
	 */
	protected boolean isRunning() {
		return running && thread != null;
	}

	/**
	 * 判断线程处于停止状态
	 * @return 返回真或者假
	 */
	protected boolean isStopped() {
		return !isRunning();
	}

	/**
	 * 启动线程，在启动线程前调用"init"方法
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
		wakeup();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 锁定关联
		writer.asPushThread(true);
		// 进入线程工作
		setRunning(true);

		// 如果线程被要求中断，退出循环
		while (!isInterrupted()) {
			// 处理子包和检查
			int process = process();
			int check = check();
			// 上面两个方法都没有处理，延时
			if (process == 0 && check == 0) {
				delay(1000);
			}
		}

		// 通知REPLY HELPER删除自己
		helper.removeReceiver(replyFlag);

		// 退出线程
		setRunning(false);
		// 解除关联
		writer.asPushThread(false);
		
		thread = null;
	}

	/**
	 * 检查参数
	 * @return 返回检查的数目
	 */
	private int check() {
		// 失效，释放自己！
		if (checkDisableTimeout()) {
			return 1;
		}

		// 如果处于锁定状态，只检查CAST-OKAY超时，其它不处理
		if (isLock()) {
			boolean success = checkLockTimeout();
			return (success ? 1 : 0);
		}

		// 存在重试超时或者断裂
		boolean success = checkRetryTimeout();
		if (!success) {
			success = checkBreakTimeout();
		}
		return (success ? 1 : 0);
	}

	/**
	 * 判断已经失效
	 * @return 返回真或者假
	 */
	private boolean checkDisableTimeout() {
		boolean success = isPacketTimeout(ReplyHelper.getDisableTimeout());
		if (success) {
			Logger.error(this, "checkDisableTimeout", "delete disabled, Cast Code: %s, disable timeout: %d",
					getCastCode(), ReplyHelper.getDisableTimeout());
			setInterrupted(true);
		}
		return success;
	}
	
	/**
	 * 判断锁定超时，超时后重新发送"CAST-OKAY"包
	 * @return 发送返回真，否则假
	 */
	private boolean checkLockTimeout() {
		// 如果锁定超，再发包
		boolean success = isLockTimeout(ReplyHelper.getSubPacketTimeout());
		if (success) {
//			Logger.error(this, "checkLockTimeout", "post \'Lock-cast-okay\', Packet id：%d, Cast code：%s, SubPacket timeout: %d",
//					packetId, getCastCode(), ReplyHelper.getSubPacketTimeout());
			success = lock_next(replyFlag.getRemote(), packetId, 1);
			if (success) {
				refreshLockTime(); // 更新锁定时间
			}
		}
		return success;
	}

	/**
	 * 判断是FIXP组包超时（是FIXP子包的组合）
	 * @return 返回真或者假
	 */
	private boolean checkRetryTimeout() {
		boolean success = isPacketTimeout(ReplyHelper.getSubPacketTimeout())
				&& isRetryTimeout(ReplyHelper.getSubPacketTimeout());
		if (success) {
//			Logger.error(this, "checkRetryTimeout", "重传FIXP全部子包！通信码：%s，超时时间：%d", getCastCode(), ReplyHelper.getSubPacketTimeout());
			retry();
		}
		return success;
	}

	/**
	 * 子包序列存在断裂，且达到1秒超时
	 * @return 返回真或者假
	 */
	private boolean checkBreakTimeout() {
		boolean success = (basket.isBreak() && isRetryTimeout(1000));
		if (success) {
//			Logger.error(this, "checkBreakTimeout", "FIXP子包断裂重传！通信码：%s", getCastCode());
			retry();
		}
		return success;
	}
		
}