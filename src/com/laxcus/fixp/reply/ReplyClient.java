/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.io.*;
import java.net.*;
import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 异步数据反馈客户机。<br>
 * 以独立和非注册ReplyWorker模式，向ReplySucker发送数据流。<br><br>
 * 
 * 为保证数据成功发送/接收，和避免中继设备分割数据包，UDP重要准则是数据包尺寸，稳妥方案是将包限制在INTERNET的IP MTU的576字节以内，若考虑包头长度约100字节，实际数据域只有476字节。此种方案会增加服务器的接收压力，造成服务器多个线程频繁交互。<br><br>
 * 另一种方案是增大数据包的尺寸，以UDP MTU的65507为上限，把数据包尺寸限制在这个范围内。如前所述，这会造成中继设备拆分/重组数据包，降低发送成功率。但是服务器接收压力降低，线程交互频率减少。<br><br>
 * 实际处理中，应根据网络环境，用系统提供的SWARM/MULTI SWARM/GUST/MULTI GUST命令来检测网络环境，确定数据包尺寸。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/23/2018
 * @since laxcus 1.0
 */
public class ReplyClient extends ReplyProvider {

	/** FIXP监听套接字 */
	private DatagramSocket socket;

	/** 接收缓存 **/
	private byte[] receiveBuff;

	/** SOCKET接收缓冲区 */
	private int receiveBufferSize;

	/** SOCKET发送缓存 **/
	private int sendBufferSize;

	/** 拒绝流量控制 **/
	private boolean refuseFlowControl;

	/** 最后驻留时间 **/
	private volatile long stayTime;

	/** 子包统计数目 **/
	private int subPackets;

	/** 超时统计 **/
	private int timeoutCount;

	/** 发生重传次数 **/
	private int retries;

	/** 客户机编号，保证唯一 **/
	private long clientId;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.reply.ReplyWatcher#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		close();
		socket = null;
		receiveBuff = null;
	}

	/**
	 * 构造快速FIXP数据包发送器，指定异步通信令牌
	 * @param token 异步通信令牌
	 */
	public ReplyClient(CastToken token) {
		super();
		setToken(token);

		// 套接字接收缓存，默认最大空间！
		receiveBuff = new byte[ReplyTransfer.MTU];

		// 发送器的发送缓存是2M，接收1M。
		setReceiveBufferSize(0x100000);
		setSendBufferSize(0x200000);
		// 设置接收超时时间
		setReceiveTimeout(ReplyWorker.getDisableTimeout());

		// 驻留开始时间
		stayTime = System.currentTimeMillis();

		// 子包统计数目
		subPackets = 0;
		// 超时统计
		timeoutCount = 0;
		// 重传次数
		retries = 0;

		// 序列号
		nextSerial();

		// 默认是假，随控制端调整
		setRefuseFlowControl(false);
	}

	/**
	 * 设置拒绝流量控制
	 * @param b 真或者假
	 */
	public void setRefuseFlowControl(boolean b) {
		refuseFlowControl = b;
	}

	/**
	 * 判断是拒绝流量控制
	 * @return 真或者假
	 */
	public boolean isRefuseFlowControl() {
		return refuseFlowControl;
	}

	/**
	 * 分配一个序列号，保证全局唯一！
	 */
	private void nextSerial() {
		clientId = generator.nextSerial();
	}

	/**
	 * 返回超时发生次数
	 * 
	 * @return 超时次数
	 */
	public int getTimeoutCount() {
		return timeoutCount;
	}

	/**
	 * 返回发送的FIXP子包数目
	 * @return 子包数目
	 */
	public int getSubPackets() {
		return subPackets;
	}

	/**
	 * 返回重传次数
	 * @return 重传次数
	 */
	public int getRetries() {
		return retries;
	}

	/**
	 * 判断已经绑定
	 * @return 返回真或者假
	 */
	public boolean isBound() {
		return (socket != null && socket.isBound());
	}

	/**
	 * 判断已经关闭
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		return (socket == null || socket.isClosed());
	}

	/**
	 * 设置接收缓冲区尺寸
	 * @param size 缓冲区尺寸
	 */
	public void setReceiveBufferSize(int size) {
		if (size > 0) {
			receiveBufferSize = size;
		}
	}

	/**
	 * 返回接收缓冲区尺寸
	 * @return 缓冲区尺寸
	 */
	public int getReceiveBuffSize() {
		return receiveBufferSize;
	}

	/**
	 * 设置发送缓冲区尺寸
	 * @param size
	 */
	public void setSendBufferSize(int size) {
		if (size > 0) {
			sendBufferSize = size;
		}
	}

	/**
	 * 返回发送缓冲区尺寸
	 * @return
	 */
	public int getSendBuffSize() {
		return sendBufferSize;
	}

	/**
	 * 返回本地套接字地址
	 * @return 套接字地址，如果socket关闭返回空指针
	 */
	public SocketHost getLocal() {
		// 如果关闭连接，返回空指针
		if (isClosed()) {
			return null;
		}

		// 本地IP和本地端口
		InetAddress address = socket.getLocalAddress();
		int port = socket.getLocalPort();
		// 生成不受沙箱检查的主机地址，再转化为可被沙箱检查
		ShadowHost host = new ShadowHost(SocketTag.UDP, address, port);
		return host.getSocketHost();
	}

	/**
	 * 返回连接的主机地址
	 * @return SocketHost实例，或者空指针
	 */
	private SocketHost getRemote() {
		// 如果关闭连接，返回空指针
		if (isClosed()) {
			return null;
		}
		return getToken().getListener();
	}

	/**
	 * 设置SOCKET接收等待时间，执行“receive”进入阻塞时，超时弹出异常
	 * @return 设置成功返回真，否则假
	 */
	private boolean setSoTimeout() {
		// 非关闭状态，重设接收超时时间
		boolean allow = (!isClosed() && feedbackTimeout > 0);
		if (!allow) {
			return false;
		}
		try {
			int ms = socket.getSoTimeout();
			// 两个时间不一样时才重置
			if (ms != feedbackTimeout) {
				socket.setSoTimeout(feedbackTimeout);
				return true;
			}
		} catch (SocketException e) {
			Logger.error(e);
		}
		return false;
	}

	/**
	 * 建立UDP套接字，绑定到本地地址，同时不连接到指定的地址，即不调用DatagramSocket.connect方法。<br>
	 * 如果传入地址是空指针，地址的分配和绑定由系统处理。这里通常绑定的是通配符地址（全0地址）。
	 * 
	 * @param local 本地套接字地址
	 * @return 成功返回真，否则假
	 */
	public boolean bind(SocketAddress local) {
		// 如果没有指定本地地址，选择通配符地址，端口由系统分配
		if (local == null) {
			local = new InetSocketAddress(new Address().getInetAddress(), 0);
		}

		boolean success = false;
		// 锁定
		try {
			socket = new DatagramSocket(null);
			// 绑定本地地址
			socket.bind(local);

			// 设置接收缓冲区尺寸
			if (receiveBufferSize > 0) {
				socket.setReceiveBufferSize(receiveBufferSize);
			}
			// 设置发送缓冲区尺寸
			if (sendBufferSize > 0) {
				socket.setSendBufferSize(sendBufferSize);
			}
			// 设置子包接收超时
			setSoTimeout();

			// 打印日志
//			bindLog();

			success = true;
		} catch (SocketException e) {
			Logger.error(e);
		}
		// 成功或者失败
		return success;
	}

//	private void bindLog() {
//		try {
//			Logger.debug(this, "bindLog", "调用器编号 :%d, 本地绑定: %s, 发送到: %s, 发送缓存:%s - %s, 接收缓存:%s - %s",
//					clientId, getLocal(), getRemote(),
//					ConfigParser.splitCapacity(sendBufferSize), ConfigParser.splitCapacity(socket.getSendBufferSize()),
//					ConfigParser.splitCapacity(receiveBufferSize), ConfigParser.splitCapacity(socket.getReceiveBufferSize()));
//		} catch (SocketException e) {
//			Logger.error(e);
//		}
//	}

	/**
	 * 绑定到指定的地址，端口号默认是0，由系统随机选择。
	 * @param local 本地IP地址
	 * @return 成功返回真，否则假
	 */
	public boolean bind(Address local) {
		SocketAddress address = null;
		if (local != null) {
			address = new InetSocketAddress(local.getInetAddress(), 0);
		}
		return bind(address);
	}

	/**
	 * 建立SOCKET，同时绑定本地地址
	 * @param local 本地SOCKET地址，不允许空指针
	 * @return 成功返回真，否则假
	 */
	public boolean bind(SocketHost local) {
		InetSocketAddress address = null;
		if (local != null) {
			address = new InetSocketAddress(local.getInetAddress(), local.getPort());
		}
		return bind(address);
	}

	/**
	 * 系统分配和绑定本地地址
	 * @return 成功返回真，否则假
	 */
	public boolean bind() {
		SocketAddress address = null;
		return bind(address);
	}

//	private void closeLog() {
//		// 打印日志
//		Logger.info(this, "closeLog", "调用器编号 :%d, 关闭SOCKET! 本地绑定:%s, 连接到:%s",
//				clientId,  getLocal(), getRemote());
//	}

	/**
	 * 关闭套接字
	 */
	public void close() {
		// 释放本地的SOCKET
		if (isClosed()) {
			return;
		}

//		closeLog();

		// 关闭
		try {
			socket.disconnect();
			socket.close();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			socket = null;
		}
	}


	/**
	 * 接收FIXP数据包
	 * @return FIXP包实例
	 * @throws IOException - SOCKET超时，或者其它...
	 */
	protected Packet receiveFrom() throws SecureException, IOException {
		// 判断套接字失效
		if (isClosed()) {
			throw new SocketException("socket closed!");
		}
		// 从服务器接收数据
		DatagramPacket udp = new DatagramPacket(receiveBuff, 0, receiveBuff.length);
		// 可能发生接收超时
		socket.receive(udp);

		// 来源地址
		SocketHost from = new ShadowHost(SocketTag.UDP, udp.getAddress(), udp.getPort()).getSocketHost();

		// 接收UDP数据包
		byte[] data = udp.getData();
		int off = udp.getOffset();
		int len = udp.getLength();
		if (len < 1) {
			throw new IOException("illegal datagram packet!");
		}

		// 统计接收流量
		addReceiveFlowSize(len);

		// 取实际数据
		byte[] b = Arrays.copyOfRange(data, off, off + len);

		// 解析数据包并且返回
		return new Packet(from, b, 0, b.length);
	}

	/**
	 * 读取一个FIXP UDP数据包。<br><br>
	 * 
	 * 错误处理：<br>
	 * 如果超时循环到下次，其它错误是退出。<br>
	 * 
	 * @return 返回包实例，或者空指针
	 */
	private Packet readPacket() {
		int count = 0;
		while(count < getReceiveTimeout()) {
			// 判断关闭
			if(isClosed()) {
				break;
			}
			try {
				return receiveFrom();
			} catch (SocketTimeoutException e) {
				// 统计超时次数
				timeoutCount++;
				// 记录日志
				Logger.error(e, "fixp packet timeout: %d", feedbackTimeout);
				// 统计值增1
				count += feedbackTimeout;
			} catch (SecureException e) {
				Logger.error(e);
				break;
			} catch (IOException e) {
				Logger.error(e);
				break;
			} catch (Throwable e) {
				Logger.fatal(e);
				break;
			}
		}
		return null;
	}

	/**
	 * 解析数据流
	 * @param packet
	 */
	private void doFlowControl(Packet packet) {
		// 子包等待超时时间
		Integer iTimeout = packet.findInteger(MessageKey.SUBPACKET_FEEDBACK_TIMEOUT);
		if (iTimeout == null) {
			return;
		}
		setFeedbackTimeout(iTimeout.intValue()); // 超时，单位：毫秒
		// 重置超时时间
		setSoTimeout();

		// 如果拒绝流量控制，以下参数将忽略不处理
		if (isRefuseFlowControl()) {
			return;
		}

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

		// 设置参数
		setMode(iMode.intValue()); // 传输模式
		setSendUnit(iUnit.intValue()); // 单位发送子包数目
		setSubPacketSize(iSize.intValue()); // 单个子包尺寸
		setSendInterval(iInterval.intValue()); // 发送间隔时间：单位：毫秒
	}

	/**
	 * 执行握手操作，是快速数据传输的第一步。<br>
	 * 
	 * 是客户端发起，服务端接收，确定客户机地址来源，服务端反馈OKAY命令结果。
	 * 
	 * @return 成功返回真，否则假
	 */
	public boolean hello() {
		Packet packet = new Packet(Ask.NOTIFY, Ask.CASTHELO);
		packet.addMessage(MessageKey.CAST_FLAG, getCastFlag());
		packet.setRemote(getToken().getListener());

		// 向目标发送数据包
		boolean success = false;
		try {
			sendTo(packet, 3);
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}
		if (!success) {
			return false;
		}

		// 接收数据
		while (true) {
			packet = readPacket();
			// 返回空包，退出！
			if (packet == null) {
				break;
			}
			Mark mark = packet.getMark();
			if (Answer.isCastHeloOkay(mark)) {
				// 解析流量控制信息
				doFlowControl(packet);

				return true;
			} else {
				try {
					sendTo(packet, 1);
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}
		return false;
	}

	/**
	 * 结束会话，双方关闭SOCKET
	 * @param num 发送数目
	 * @return 发送成功且收到反馈，返回真，否则假
	 */
	public boolean exit(int num) {
		if (num < 1) {
			throw new IllegalValueException("illegal param:%d", num);
		}
		Packet packet = new Packet(Ask.NOTIFY, Ask.CASTEXIT);
		packet.setRemote(getToken().getListener());
		packet.addMessage(MessageKey.CAST_CODE, getCastCode());
		packet.addMessage(MessageKey.SPEAK, "TO RECEIVER!");

		// 连续发送N个数据包
		try {
			sendTo(packet, num);
		} catch (IOException e) {
			Logger.error(e);
		}
		// 接收数据包
		boolean success = false;
		while (true) {
			// 接收包
			packet = readPacket();
			if (packet == null) {
				break;
			}
			// 判断是请求端发送的退出
			success = Answer.isCastExitOkay(packet.getMark());
			if (success) {
				break;
			}
		}
		return success;
	}

	/**
	 * 在收到“CAST_OKAY”后，向来源地址返回“CAST_OKAY_REPLY”，形成三段结果。<br>
	 * 三段： ReplyReceiver(CAST_OKAY) -> ReplyClient(CAST_OKAY_REPLY) -> ReplyReceiver <br>
	 * 
	 * @param sendId 包编号
	 * @return 成功返回真，否则假
	 */
	private boolean confirm(int sendId, int count) {
		if (count < 1) {
			count = 1;
		}

		Packet packet = new Packet(Answer.CAST_OKAY_REPLY);
		packet.setRemote(getToken().getListener());
		packet.addMessage(MessageKey.CAST_CODE, getCastCode());
		packet.addMessage(MessageKey.PACKET_IDENTIFY, sendId);
		packet.addMessage(MessageKey.SPEAK, "From ReplyClient!");

		// 生成命令，向来源返回，形成三段结果
		boolean success = false;
		try {
			sendTo(packet, count);
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}
		return success;
	}

	/**
	 * 向目标地址发送退出包
	 * @return 成功返回真，否则假
	 */
	public boolean exit() {
		return exit(5);
	}

//	private void timeoutLog(SocketTimeoutException e) {
//		// 打印日志
//		Logger.error(e, "接收超时！调用器编号:%d, 本地绑定:%s, 连接到:%s, 重新发送全部FIXP子包！超时时间：%d | %s",
//				clientId, getLocal(), getRemote(), feedbackTimeout, getCastCode());
//	}

	/**
	 * 处理反馈包，必须等到有应答后才退出。<br><br>
	 * 
	 * 几种情况：<br>
	 * 1. 撤销包，表示接收端收到全部数据，宣布本次工作完成。<br>
	 * 2. 重发包，要求发送包集合中的子包。<br>
	 * 3. 超时，重新发送数据包<br>
	 * 
	 * @param packets 子包数目
	 * @return 成功返回真，否则假
	 */
	private boolean receive(List<Packet> packets) {
		int timeouts = 0; 
		while (timeouts < getReceiveTimeout()) {
			// 读FIXP数据包
			Packet packet = null;
			try {
				packet = receiveFrom();
			} catch (SocketTimeoutException e) {
				// 统计超时次数
				timeoutCount++;

//				timeoutLog(e);

				// 子包超时统计
				timeouts += feedbackTimeout;
				// 超时，重新发送FIXP子包集合
				int index = sendPackets(packets);
				// 判断重发成功
				if (index == packets.size()) {
					retries += index; // 增加重传次数
					continue;
				}
			} catch (SecureException e) {
				Logger.error(e);
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
			// 以上不成功，退出循环
			if (packet == null) {
				break;
			}

			// 取得标记
			Mark mark = packet.getMark();

			// 收到来自ReplyReceiver的CAST_OKAY包，判断编号一致结束本次通信，同时要处理冗余
			if (Answer.isCastOkay(mark)) {
				// 解析流量控制
				doFlowControl(packet);

				// 包编号
				Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
				if (id == null) {
					continue;
				}
				int fromId = id.intValue();
				// 包编号一致，返回"CAST_OKAY_REPLY"应答包，包编号递增1
				if (packetId == fromId) {
					boolean success = confirm(packetId, 1);
					if (success) {
						packetId += 1;
						return true; // 包编号加1后退出，启动下一轮发送
					} else {
						continue;
					}
				} else {
					continue;// 其它情况，忽略
				}
			}
			// 收到来自ReplyReceiver的LOCK_CAST_OKAY包，判断编号一致结束本次通信
			else if (Answer.isLockCastOkay(mark)) {
				// 解析流量控制
				doFlowControl(packet);

				// 包编号
				Integer id = packet.findInteger(MessageKey.PACKET_IDENTIFY);
				if (id == null) {
					continue;
				}
				int fromId = id.intValue();
				// ReplyReceiver在锁定状态发出，包编号一致，返回"CAST_OKAY_REPLY"应答包，包编号递增1，退出。
				if (packetId == fromId) {
					boolean success = confirm(fromId, 1);
					if (success) {
						packetId += 1;
						return true; // 包编号加1后退出，启动下一轮发送
					} else {
						continue;
					}
				}				
				// 之前的“CAST_OKAY_REPLY”没有收到，ReplyReceiver在锁定状态发出，这里再发一次
				else if (packetId == fromId + 1) {
					confirm(fromId, 1);// 再次确认，继续等待本次处理
					continue;
				} else {
					continue; // 其它情况，忽略！
				}
			}
			// 发送全部FIXP包
			else if (Assert.isCastRefire(mark)) {
				// 解析流量控制
				doFlowControl(packet);

				// 判断包编号
				Integer remoteId = packet.findInteger(MessageKey.PACKET_IDENTIFY);
				boolean match = (remoteId != null && remoteId.intValue() == packetId);
				// 不是本批次数据包，忽略它
				if (!match) {
					continue;
				}
				// 重新发送全部子包
				for (Packet sub : packets) {
					try {
						sendTo(sub);
						retries++; // 重传统计加1
					} catch (IOException e) {
						Logger.error(e);
					}
				}
			}
			// 是重发包
			else if (Assert.isRetrySubPacket(mark)) {
				// 解析流量控制
				doFlowControl(packet);

				Integer remoteId = packet.findInteger(MessageKey.PACKET_IDENTIFY);
				boolean match = (remoteId != null && remoteId.intValue() == packetId);
				// 不是本批次数据包，忽略它
				if (!match) {
					continue;
				}
				// 取子包编号
				List<Integer> serials = getSerials(packet, packets.size());
				// 子包序号是从0开始，逐一取出，发送给请求端
				for(int serial : serials) {
					Packet sub = packets.get(serial);

//					retryLog(serial, sub, packet);

					try {
						sendTo(sub);
						retries++; // 重传统计加1 
					} catch (IOException e){
						Logger.error(e);
					}
				}
			}
		}

		Logger.error(this, "receive", "cannot be receive! retries:%d, timeouts:%d, %s",
				retries, timeouts, getCastCode());

		return false;
	}

//	private void retryLog(int serial, Packet to, Packet from) {
//		Logger.debug(this, "retryLog", "调用器编号 :%d, 本地绑定:%s, 连接到:%s, 延时驻留：%d ms, 客户端反馈子包:%d, 长度:%d，来自: %s",
//				clientId, getLocal(), getRemote(), sendInterval, serial, to.getContentLength(), from.getRemote());
//	}

	/**
	 * 发送FIXP子包
	 * @param packet FIXP子包
	 * @return 成功返回真，否则假
	 */
	private void __sendTo(Packet packet) throws IOException {
		// 如果套接字失效，弹出异常
		if (isClosed()) {
			throw new SocketException("socket closed!");
		}

		// 输出字节数组
		byte[] b = packet.build();
		// 指定发送目标
		SocketAddress remote = getToken().getListener().getSocketAddress();
		
		// 生成UDP包发送
		DatagramPacket udp = new DatagramPacket(b, 0, b.length);
		udp.setSocketAddress(remote);
		// 发送UDP数据包
		socket.send(udp);
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
	 * @return 返回发送的子包数目
	 */
	private int __sendTo(List<Packet> packets) throws IOException {
		int size = packets.size();
		int index = 0;
		// 循环发送
		while (index < size) {
			// 无论并行/串行处理，发送前都进行延时
			stay();

			// 选择最小单位
			int left = (size - index > sendUnit ? sendUnit : size - index);
			List<Packet> subs =	packets.subList(index, index + left);

			// 判断是并行模式发送
			if (ReplyTransfer.isParallelTransfer(mode)) {
				for (Packet sub : subs) {
					__sendTo(sub);
					index++; // 成功，统计值加1
				}
			}
			// 以串行模式发送
			else {
				SocketHost host = getRemote();
				// 加串行锁，
				FlowLock.getInstance().lock(host, clientId);
				for(Packet sub : subs) {
					__sendTo(sub);
					index++; // 成功，统计值加1
				}
				// 串行解锁
				FlowLock.getInstance().unlock(host, clientId);
			}
		}
		return index;
	}

	/**
	 * 发送一批数据包
	 * @param packets FIXP数据子包集合
	 * @return 返回发送的数目
	 */
	private int sendPackets(List<Packet> packets) {
		// 逐一发送数据包
		int index = 0;
		// 发送一批数据包
		try {
			index = __sendTo(packets);
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		return index;
	}

	/**
	 * 向目标站点发送数据包。目标地址已经连接！
	 * @param packet FIXP数据包
	 * @param count 发送次数
	 * @throws IOException
	 */
	protected void sendTo(Packet packet, int count) throws IOException {
		ArrayList<Packet> array = new ArrayList<Packet>();
		array.add(packet);

		for (int i = 0; i < count; i++) {
			__sendTo(array);
		}
	}

	/**
	 * 向目标站点发送数据包
	 * @param packet FIXP数据包
	 * @throws IOException
	 */
	protected void sendTo(Packet packet) throws IOException {
		// 发送数据包
		sendTo(packet, 1);
	}

	/**
	 * 发送一组数据到目标站点。<br>
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 数据有效长度
	 * @param unit 子包单元长度
	 * @return 成功返回真，否则假
	 */
	public boolean send(byte[] b, int off, int len, int unit) {
		// 选择最小尺寸
		unit = Math.min(unit, getSubPacketSize());

		// 切割数据，分成多个子包
		List<Packet> packets = null;
		try {
			packets = split(b, off, len, unit);
		} catch (SecureException e) {
			Logger.error(e);
		}
		if (packets == null) {
			return false;
		}

		// 记录重试次数
		final int scale = retries;

		// 发送一批数据子包
		int count = sendPackets(packets);

		// 判断发送成功
		boolean success = (count == packets.size());
		// 接收反馈结果
		if (success) {
			success = receive(packets);
		}
		// 成功，参数增加
		if (success) {
			// 统计子包数目（有效发送的）
			subPackets += packets.size();
		}

		// 以上成功，且发生重试。下调下一次子包的最小尺寸
		if (retries > scale) {
			// 不成功，调整到最小包，以下情况分别减少
			if (!success) {
				setSubPacketSize(ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE);
			} else if (getSubPacketSize() > 2048) {
				setSubPacketSize(2048);
			} else if (getSubPacketSize() > 1024) {
				setSubPacketSize(1024);
			} else {
				setSubPacketSize(ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE);
			}
			// Logger.warning(this, "send", "descend subpacket size: %d",
			// getSubPacketSize());

			Logger.warning(this, "send", "减少FIXP子包尺寸到: %d", getSubPacketSize());
		}

		return success;
	}

	/**
	 * 发送一组数据到目标站点。<br>
	 * 
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 数据有效长度
	 * @return 成功返回真，否则假
	 */
	public boolean send(byte[] b, int off, int len) {
		int unit = ReplyTransfer.getDefaultSubPacketContentSize();
		return send(b, off, len, unit);
	}

}


//	/** 发送模式 **/
//	private volatile int mode;
//	
//	/** 单次发送FIXP子包数目 **/
//	private volatile int sendUnit;
//	
//	/** FIXP子包发送间隔 **/
//	private volatile int sendInterval;
//	
//	/** 子包发送后等待返回结果的超时时间，默认是10秒 */
//	private volatile int feedbackTimeout;


//	/**
//	 * 返回默认的子包尺寸
//	 * @return 子包尺寸
//	 */
//	public int getSubPacketSize() {
//		return subPacketSize;
//	}
//
//	/**
//	 * 设置默认的子包尺寸
//	 * @param len 指定长度
//	 * @return 设置成功返回真，否则假
//	 */
//	public boolean setSubPacketSize(int len) {
//		boolean success = (len > 0 && len < ReplyTransfer.maxSubPacketSize);
//		if (success) {
//			subPacketSize = len;
//		}
//		return success;
//	}

//	/**
//	 * 设置UDP数据报文接收超时时间。如果为0值 ，是无限等待。
//	 * @param ms 毫秒
//	 * @return 设置成功返回真，否则假
//	 */
//	public boolean setSoTimeout(int ms) {
//		try {
//			if (!isClosed() && ms > 0) {
//				socket.setSoTimeout(ms);
//				return true;
//			}
//		} catch (SocketException e) {
//			Logger.error(e);
//		}
//		return false;
//	}


///**
// * 返回连接的主机地址
// * @return SocketHost实例，或者空指针
// */
//public SocketHost getRemote() {
//	// 如果关闭连接，返回空指针
//	if (isClosed()) {
//		return null;
//	}
//	InetAddress address = socket.getInetAddress();
//	int port = socket.getPort();
//	// 生成不受沙箱检查的主机地址，再转化为可被沙箱检查
//	ShadowHost host = new ShadowHost(SocketTag.UDP, address, port);
//	return host.getSocketHost();
//}




//	/**
//	 * 发送前的延时驻留
//	 */
//	private void parallelStay() {
//		if (sendInterval > 0) {
//			if (System.currentTimeMillis() - stayTime < sendInterval) {
//				delay(sendInterval);
//				stayTime = System.currentTimeMillis();
//			}
//		}
//	}
//
//	/**
//	 * 发送前的延时驻留
//	 */
//	private void serialStay() {
//		if (sendInterval > 0) {
//			delay(sendInterval);
//		}
//	}

//	/**
//	 * 以串行方式发送UDP数据包到目标地址
//	 * @param udp UDP数据包
//	 */
//	private void serialSendTo(DatagramPacket udp) {
//		// 发送前串行延时...
//		stay();
//		
//		SocketHost host = getRemote();
//		// 串行锁定
//		FlowLock.getInstance().lock(host, clientId);
//		// 发送UDP数据包
//		try {
//			socket.send(udp);
//		} catch (IOException e) {
//			Logger.error(e);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		// 串行解锁
//		FlowLock.getInstance().unlock(host, clientId);
//		
//		stayTime = System.currentTimeMillis();
//	}

//	/**
//	 * 以并行发送UDP数据包到目标地址
//	 * @param udp UDP数据包
//	 * @throws IOException
//	 */
//	private void parallelSendTo(DatagramPacket udp) throws IOException {
//		//		parallelStay();
//		//		serialStay();
//		
//		// 延时，发包、记录时间
//		stay();
//		socket.send(udp);
//		stayTime = System.currentTimeMillis();
//	}

//	/**
//	 * 向目标站点发送数据包。目标地址已经连接！
//	 * @param packet FIXP数据包
//	 * @param count 发送次数
//	 * @throws IOException
//	 */
//	protected void sendTo(Packet packet, int count) throws IOException {
//		// 如果套接字失效，弹出异常
//		if (isClosed()) {
//			throw new SocketException("socket closed!");
//		}
//
//		// 输出字节数组
//		byte[] b = packet.build();
//
//		DatagramPacket udp = new DatagramPacket(b, 0, b.length);
//
//		for (int i = 0; i < count; i++) {
//			// 判断是并行或者串行模式
//			if (ReplyTransfer.isSerialTransfer()) {
//				serialSendTo(udp);
//			} else {
//				parallelSendTo(udp);
//			}
//			// 统计发送流量
//			addSendFlowSize(b.length);
//		}
//
//		// 发送指定的数目
//		//		for (int i = 0; i < count; i++) {
//		//			// 发送间隔，避免接收端压力过大，减少UDP丢包，
//		//			stay();
//		//			// 数据包
//		//			socket.send(udp);
//		//			// 统计发送流量
//		//			addSendFlowSize(b.length);
//		//		}
//	}


//	/**
//	 * 发送一批数据包
//	 * @param packets FIXP数据子包集合
//	 * @return 返回发送的数目
//	 */
//	private int sendPackets(List<Packet> packets){
//		// 逐一发送数据包
//		int index = 0;
//		for (; index < packets.size(); index++) {
//			Packet packet = packets.get(index);
//			try {
//				sendTo(packet);
//			} catch (IOException e) {
//				Logger.error(e);
//				break;
//			}
//		}
//		return index;
//	}

///**
//* 返回连接的主机地址
//* @return SocketHost实例，或者空指针
//*/
//private SocketHost getRemote() {
//	// 如果关闭连接，返回空指针
//	if (isClosed()) {
//		return null;
//	}
//	return getToken().getListener();
//	
////	InetAddress address = socket.getInetAddress();
////	int port = socket.getPort();
////	// 生成不受沙箱检查的主机地址，再转化为可被沙箱检查
////	ShadowHost host = new ShadowHost(SocketTag.UDP, address, port);
////	return host.getSocketHost();
//}

///**
//* 建立UDP套接字，绑定到本地地址，同时不连接到指定的地址，即不调用DatagramSocket.connect方法。<br>
//* 如果传入地址是空指针，地址的分配和绑定由系统处理。这里通常绑定的是通配符地址（全0地址）。
//* 
//* @param local 本地套接字地址
//* @return 成功返回真，否则假
//*/
//public boolean bind(SocketAddress local) {
//	// 如果没有指定本地地址，选择通配符地址，端口由系统分配
//	if (local == null) {
//		local = new InetSocketAddress(new Address().getInetAddress(), 0);
//	}
//
//	boolean success = false;
//	// 锁定
//	try {
//		socket = new DatagramSocket(null);
//		// 绑定本地地址
//		socket.bind(local);
//
//		// 设置接收缓冲区尺寸
//		if (receiveBufferSize > 0) {
//			socket.setReceiveBufferSize(receiveBufferSize);
//		}
//		// 设置发送缓冲区尺寸
//		if (sendBufferSize > 0) {
//			socket.setSendBufferSize(sendBufferSize);
//		}
//		// 设置子包接收超时
//		setSoTimeout();
//
////		// 连接到目标地址
////		SocketAddress remote = getToken().getListener().getSocketAddress();
////		socket.connect(remote);
//
//		// 打印日志
////		bindLog();
//
//		success = true;
//	} catch (SocketException e) {
//		Logger.error(e);
//	}
//	// 成功或者失败
//	return success;
//}

///**
//* 解析数据流
//* @param packet
//*/
//private void doFlowControl(Packet packet) {
//	// 子包等待超时时间
//	Integer iTimeout = packet.findInteger(MessageKey.SUBPACKET_FEEDBACK_TIMEOUT);
//	if (iTimeout == null) {
//		return;
//	}
//	setFeedbackTimeout(iTimeout.intValue()); // 超时，单位：毫秒
//	// 重置超时时间
//	setSoTimeout();
//
//	// 如果拒绝流量控制，以下参数将忽略不处理
//	if (isRefuseFlowControl()) {
//		return;
//	}
//
//	// 传输模式
//	Integer iMode = packet.findInteger(MessageKey.SUBPACKET_TRANSFER_MODE);
//	if (iMode == null) {
//		return;
//	}
//	// 一次方法调用发送的子包数目
//	Integer iUnit = packet.findInteger(MessageKey.SUBPACKET_UNIT);
//	if (iUnit == null) {
//		return;
//	}
//	// 单个子包内容尺寸
//	Integer iSize = packet.findInteger(MessageKey.SUBPACKET_CONTENT_SIZE);
//	if (iSize == null) {
//		return;
//	}
//	// 定义一个新的子包尺寸
//	Integer iInterval = packet.findInteger(MessageKey.SUBPACKET_SEND_INTERVAL);
//	if (iInterval == null) {
//		return;
//	}
//
//	// 设置参数
//	setMode(iMode.intValue()); // 传输模式
//	setSendUnit(iUnit.intValue()); // 单位发送子包数目
//	setSubPacketSize(iSize.intValue()); // 单个子包尺寸
//	setSendInterval(iInterval.intValue()); // 发送间隔时间：单位：毫秒
//
//	//		// 控制流
//	//		Logger.debug(this, "doFlowControl", "%s (%d#%d#%d)",
//	//				ReplyTransfer.translateTransferMode(mode), sendUnit,
//	//				getSubPacketSize(), sendInterval);
//}

///**
//* 发送一组数据到目标站点。<br>
//* 
//* @param b 字节数组
//* @param off 下标
//* @param len 数据有效长度
//* @param unit 子包单元长度
//* @return 成功返回真，否则假
//*/
//public boolean send(byte[] b, int off, int len, int unit) {
//	//		// 如果子包单元长度超过规定的子包尺寸，以规定的子包尺寸为准。
//	//		if (unit > getSubPacketSize()) {
//	//			unit = getSubPacketSize();
//	//		}
//
//	// 选择最小尺寸
//	unit = Math.min(unit, getSubPacketSize());
//
//	// 切割数据，分成多个子包
//	List<Packet> packets = null;
//	try {
//		packets = split(b, off, len, unit);
//	} catch (SecureException e) {
//		Logger.error(e);
//	}
//	if (packets == null) {
//		return false;
//	}
//
//	//		Logger.debug(this, "send", "分割后的子包数目: %d, 总长度: %d, 子包单元长度: %d, 默认子包单元: %d",
//	//			packets.size(), len, unit, subPacketSize);
//
//	// 记录重试次数
//	final int scale = retries;
//
//	// 发送一批数据子包
//	int count = sendPackets(packets);
//
//	// 判断发送成功
//	boolean success = (count == packets.size());
//	// 接收反馈结果
//	if (success) {
//		success = receive(packets);
//	}
//	// 成功，参数增加
//	if (success) {
//		//			// 下一个包编号
//		//			packetId++;
//		// 统计子包数目（有效发送的）
//		subPackets += packets.size();
//	}
//
//	//		// 以上成功，且发生重试。下调下一次子包的最小尺寸
//	//		if (success && retries > scale) {
//	//			if (getSubPacketSize() > 2048) {
//	//				setSubPacketSize(2048);
//	//			} else if (getSubPacketSize() > 1024) {
//	//				setSubPacketSize(1024);
//	//			} else {
//	//				setSubPacketSize(ReplyTransfer.minSubPacketSize);
//	//			}
//	////			Logger.warning(this, "send", "descend subpacket size: %d", getSubPacketSize());
//	//			
//	//			Logger.warning(this, "send", "减少FIXP子包尺寸到: %d", getSubPacketSize());
//	//		} 
//	//		// 不成功，调整到最小包尺寸
//	//		else if (!success) {
//	//			setSubPacketSize(ReplyTransfer.minSubPacketSize);
//	//		}
//
//	// 以上成功，且发生重试。下调下一次子包的最小尺寸
//	if (retries > scale) {
//		// 不成功，调整到最小包，以下情况分别减少
//		if (!success) {
//			setSubPacketSize(ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE);
//		} else if (getSubPacketSize() > 2048) {
//			setSubPacketSize(2048);
//		} else if (getSubPacketSize() > 1024) {
//			setSubPacketSize(1024);
//		} else {
//			setSubPacketSize(ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE);
//		}
//		// Logger.warning(this, "send", "descend subpacket size: %d",
//		// getSubPacketSize());
//
//		Logger.warning(this, "send", "减少FIXP子包尺寸到: %d", getSubPacketSize());
//	}
//
//	return success;
//}
