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

import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 异步数据发送器。<br>
 * 
 * 被异步调用器调用，以线程模式存在，向目标站点的ReplySucker发送反馈数据。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2018
 * @since laxcus 1.0
 */
public class ReplyDispatcher extends ReplyServer {

	/** 监听套接字 */
	private DatagramSocket server;

	/** 异步发送代理 **/
	private ReplyWorker worker;

	/**
	 * 构造异步数据发送器
	 */
	public ReplyDispatcher() {
		super();
		// 以发送为主，接收为辅。发送缓存16M，接收缓存3M
		setSendBufferSize(0x1000000);
		setReceiveBufferSize(0x300000);
		// 构造异步代理
		worker = new ReplyWorker(this);
	}

	/**
	 * 返回外部网络主机地址
	 * @return SocketHost实例，如果没有外部网络IP时或者没有绑定时，返回空
	 */
	public SocketHost getDefinePublicHost() {
		// 如果没有启动，返回初始定义地址
		if (server == null || definePublicIP == null) {
			return null;
		}

		SocketHost host = new SocketHost(SocketTag.UDP, definePublicIP, server.getLocalPort());
		host.setReflectPort(reflectPort);
		return host;
	}

	/**
	 * 返回内部网络主机地址
	 * @return SocketHost实例，如果没有绑定时返回空
	 */
	public SocketHost getDefinePrivateHost() {
		if (server == null || definePrivateIP == null) {
			return null;
		}

		SocketHost host = new SocketHost(SocketTag.UDP, definePrivateIP, server.getLocalPort());
		host.setReflectPort(reflectPort);
		return host;
	}

	/**
	 * 返回绑定的实际地址
	 * @return 返回SocketHost实例，没有绑定返回空指针
	 */
	public SocketHost getBindHost() {
		if (server == null) {
			return null;
		}
		return new SocketHost(SocketTag.UDP, server.getLocalAddress(), server.getLocalPort());
	}

	/**
	 * 返回异步发送辅助器
	 * @return
	 */
	public ReplyWorker getWorker(){
		return worker;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		boolean success = bind();
		if (success) {
			success = worker.start();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");

		// UDP最大尺寸: 0xffff - 20(ip head) - 8(udp head)，即65507字节。
		byte[] buff = new byte[ReplyTransfer.MTU]; 
		DatagramPacket datagram = new DatagramPacket(buff, buff.length);

		while (!isInterrupted()) {			
			try {
				// 接收UDP数据包
				server.receive(datagram);
				// 分配UDP数据包
				distribute(datagram);
			} catch (IOException e) {
				if (isInterrupted()) {
					break;
				}
				// 网络错误，打印日志 
				Logger.error(e);
			} catch (Throwable e) {
				if (isInterrupted()) {
					break;
				}
				// 故障打印日志
				Logger.fatal(e);
			}
		}

		// 释放
		datagram = null;
		buff = null;

//		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		close();
		// 停止辅助处理器并且等待完成
		ThreadStick stick = new ThreadStick();
		worker.stop(stick);
		while (!stick.isOkay()) {
			delay(200);
		}

		Logger.info(this, "finish", "exit reply dispatcher!");
	}

	/**
	 * 检测和分派UDP数据包文
	 * @param datagram
	 */
	private final void distribute(DatagramPacket datagram) {
		// 数据包的来源地址
		SocketHost from = new SocketHost(SocketTag.UDP, datagram.getAddress(), datagram.getPort());

		// 检查数据包长度
		if (datagram.getLength() < 1) {
			Logger.error(this, "distribute", "empty datagram! from %s", from);
			return;
		}

		// 检查数据包长度，保存参数
		worker.add(from, datagram.getData(), datagram.getOffset(), datagram.getLength());
	}

	/**
	 * 绑定通配符地址，允许向任意地址发送数据。
	 * @return 成功返回“真“，失败返回“假”。
	 */
	private boolean bind() {
		boolean success = false;

		// 使用通配符地址进行监听，端口由系统随机分配
		Address address = new Address(getDefinePrivateIP().isIPv4());
		SocketHost local = new SocketHost(SocketTag.UDP, address, defaultPort); 

		// 绑定通配符，端口是0，由系统分配
		SocketAddress sock = local.getSocketAddress();
		try {
			server = new DatagramSocket(null);
			server.bind(sock);
			// 设置SOCKET的接收/发送缓存
			if (getReceiveBufferSize() > 0) {
				server.setReceiveBufferSize(getReceiveBufferSize());
				Logger.debug(this, "bind", "set receive buff: %s", ConfigParser.splitCapacity(getReceiveBufferSize()));
			}
			if (getSendBufferSize() > 0) {
				server.setSendBufferSize(getSendBufferSize());
				Logger.debug(this, "bind", "set send buff: %s", ConfigParser.splitCapacity(getSendBufferSize()));
			}
			// 无限制等待数据包
			server.setSoTimeout(0); // no limit time
			success = true;
		} catch (SocketException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.error(e);
		}

		Logger.note(this, "bind", success, "default listen: %s", local);

		if (success) {
			try {
				// 判断是网关或者单主机地址
				if (isGateway()) {
					Logger.info(this, "bind", "gateway real bind: %s | %s", getDefinePublicHost(), getDefinePrivateHost());
				} else {
					Logger.info(this, "bind", "single real bind: %s", getDefinePrivateHost());
				}

				// SOCKET发送/接收缓存尺寸
				int readSize = server.getReceiveBufferSize();
				int writeSize = server.getSendBufferSize();

				Logger.info(this, "bind", "apply receive buff:%s - real receive buff:%s, apply send buff:%s - real send buff:%s",
						ConfigParser.splitCapacity(getReceiveBufferSize()), ConfigParser.splitCapacity(readSize),
						ConfigParser.splitCapacity(getSendBufferSize()), ConfigParser.splitCapacity(writeSize));
				if (getReceiveBufferSize() != readSize) {
					Logger.warning(this, "bind", "socket receive buffer, not match! %d - %d", 
							getReceiveBufferSize(), readSize);
				}
				if (getSendBufferSize() != writeSize) {
					Logger.warning(this, "bind", "socket send buffer, not match! %d - %d",
							getSendBufferSize(), writeSize);
				}
				
				// 发送SOCKET缓冲空间
				EchoTransfer.setReplyDispatcherBuffer(readSize, writeSize);
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		return success;
	}

	/**
	 * 向目标地址发送UDP包。<br>
	 * 区别于之前的锁定串行发送，为了提高效率，锁定处理已经在调用端实现，这里不再锁定。<br><br>
	 * 
	 * 
	 * 注意：这里发送的是UDP包！！！所有UDP通讯最后都是通过这个方法发送出去。
	 * 
	 * @param remote 目标地址
	 * @param b FIXP数据包字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 发送成功返回真，否则假。
	 */
	protected boolean sendTo(DatagramPacket packet) {
		boolean success = false;
		// 采用并发的方式发送UDP包，提高发送效率。串行已经在调用端实现
		try {
			if (server != null) {
				server.send(packet);
				success = true;
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} 

		//		Logger.debug(this, "sendTo", success, "%s to %s", server.getLocalSocketAddress(), packet.getSocketAddress());

		//		Logger.debug(this, "sendTo", success, "%s to %s",
		//				new SocketHost(SocketTag.UDP, server.getLocalAddress(), server.getLocalPort()),
		//				new SocketHost(SocketTag.UDP, packet.getAddress(), packet.getPort()));

		return success;
	}
	
	/**
	 * 发送一批数据包
	 * @param packets UDP数据包，内部包含地址
	 * @return 返回发送成功的数目
	 */
	protected int allTo(java.util.Collection<DatagramPacket> packets) {
		int count = 0;
		// 发送数据包
		for (DatagramPacket packet : packets) {
			boolean success = sendTo(packet); // 发送给目标地址
			if (success) count++; // 发送成功，统计加1
		}
		// 返回成功数目
		return count;
	}
	
	/**
	 * 退出线程，关闭套接字监听服务
	 * @see com.laxcus.thread.VirtualThread#stop()
	 */
	public void stop() {
		super.stop();
		close();
	}

	/**
	 * 退出线程，关闭套接字监听。结束时通知线程转发器
	 * @see com.laxcus.thread.VirtualThread#stop(com.laxcus.thread.ThreadStick)
	 */
	public void stop(ThreadStick e) {
		super.stop(e);
		close();
	}

	/**
	 * 关闭UDP套接字
	 */
	private void close() {
		if (server == null) {
			return;
		}
		// 关闭SOCKET
		try {
			server.close();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			server = null;
		}
	}
}
