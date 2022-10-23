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
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 数据并行接收器。<br><br>
 * 只用于直接连接的内部网络。<br>
 * 绑定在所有节点上。<br><br>
 * 
 * ReplySucker和MISucker的区别：<br>
 * 1. ReplySucker和MISucker是主从关系，ReplySucker为主，MISucker为副。<br>
 * 2. ReplySucker只能有一个，MISucker允许任意多个。<br>
 * 3. ReplySucker处理公网/内网通信，MISucker只处理内网通信。<br><br>
 * 
 * 双方合作，实现数据分流和再聚合，实现Massive MIMO通信。<br>
 * 
 * 关于SOCKET缓存尺寸：<br>
 * 在LINUX平台，如果用户定义的接收/发送缓存尺寸不能够生效，<br>
 * 解决的办法是“vim /etc/sysctl.conf”打开文件，写入“net.core.rmem_max=204800”和“net.core.wmem_max=102400”，<br>
 * 后缀以字节计，所有SOCKET将默认启用这个缓存尺寸。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 12/2/2020
 * @since laxcus 1.0
 */
public class MISucker extends ReplyServer {

	/** 监听套接字 */
	private DatagramSocket server;

	/** 应答辅助器，根据数据包的来源，写入各个异步调用器缓存 **/
	private ReplyHelper helper;

	/**
	 * 构造默认的数据并行接收器
	 */
	public MISucker() {
		super();
		// 以接收为主，发送为辅。接收缓存16M，发送缓存3M。
		setReceiveBufferSize(0x1000000);
		setSendBufferSize(0x300000);
	}

	/**
	 * 构造数据并行接收器，指定代理
	 * @param helper 应答辅助器
	 */
	public MISucker(ReplyHelper helper) {
		this();
		setHelper(helper);
	}
	
	/**
	 * 返回内部网络主机地址
	 * @return SocketHost实例，没有绑定时返回空
	 */
	public SocketHost getDefinePrivateHost() {
		if (server == null || definePrivateIP == null) {
			return null;
		}

		// 返回实际主机地址
		return new SocketHost(SocketTag.UDP, definePrivateIP, server.getLocalPort());
	}

	/**
	 * 返回异步接口辅助器
	 * @return ReplyHelper实例
	 */
	public ReplyHelper getHelper() {
		return helper;
	}

	/**
	 * 设置异步接口辅助器
	 * @param e ReplyHelper实例
	 */
	public void setHelper(ReplyHelper e) {
		helper = e;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 绑定SOCKET
		boolean success = bind();
		
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
				// 分配数据包
				distribute(datagram);
			} catch (IOException e) {
				if (isInterrupted()) {
					break;
				}
				// 打印错误
				Logger.error(e);
			} catch (Throwable e) {
				if (isInterrupted()) {
					break;
				}
				// 打印错误
				Logger.fatal(e);
			}
		}

		// 释放
		datagram = null;
		buff = null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		close();

		Logger.info(this, "finish", "exit Massive MI!");
	}

	/**
	 * 检查和分配UDP数据包
	 * @param udp UDP数据包
	 */
	private final void distribute(DatagramPacket udp) {
		// 数据包的来源地址
		SocketHost from = new SocketHost(SocketTag.UDP, udp.getAddress(), udp.getPort());
		// 检查数据包长度
		if (udp.getLength() < 1) {
			Logger.error(this, "distribute", "empty datagram! from %s", from);
			return;
		}

		// 保存参数
		helper.add(from, udp.getData(), udp.getOffset(), udp.getLength());
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

	/**
	 * 绑定本地套接字地址。
	 * @return 成功返回“真“，失败返回“假”。
	 */
	private boolean bind() {
		boolean success = false;

		// 使用通配符地址监听，端口由系统随机分配
		Address address = new Address(getDefinePrivateIP().isIPv4());
		SocketHost local = new SocketHost(SocketTag.UDP, address, defaultPort);

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

		// 成功，显示真实地址。
		if (success) {
			try {
				// 绑定主机地址
				Logger.info(this, "bind", "single real bind: %s", getDefinePrivateHost());

				// SOCKET接收/发送缓冲区尺寸
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

				// 给数据流分析器设置接收缓存参数
				FlowMonitor.getInstance().addSocketReceiveBufferSize(true, readSize);

				//				// 记录缓存尺寸
				//				EchoTransfer.setReplySuckerBuffer(readSize, writeSize);

			} catch (IOException e) {
				Logger.error(e);
			}
		}

		return success;
	}

	/**
	 * 向目标地址发送UDP包。<br>
	 * 注意：这里发送的是UDP包！！！所有UDP通讯最后都是通过这个方法发送出去。
	 * 
	 * @param packet 目标地址
	 * @param b FIXP数据包字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 发送成功返回真，否则假。
	 */
	protected boolean replyTo(DatagramPacket packet) {
		boolean success = false;

		// 虽然系统允许并发执行，但是为了减少SOCKET发送压力和发送成功率真，这里采用锁定的串行发送UDP包。
		super.lockSingle();
		try {
			if (server != null) {
				server.send(packet);
				success = true;
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 回传！
		//		Logger.note(this, "replyTo", success, "send to %s:%d", packet.getAddress(), packet.getPort());

		return success;
	}

	/**
	 * 向目标地址发送UDP包。为了防止竞用现象，每个UDP包在发送前都要锁定，完成后解锁。<br>
	 * 注意：这里发送的是UDP包！！！所有UDP通讯最后都是通过这个方法发送出去。
	 * 
	 * @param remote 目标地址
	 * @param b FIXP数据包字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 发送成功返回真，否则假。
	 */
	protected boolean replyTo(SocketHost remote, byte[] b, int off, int len) {
		// 客户机目标地址
		SocketAddress address = remote.getSocketAddress();
		// 生成UDP数据包
		DatagramPacket packet = new DatagramPacket(b, off, len);
		packet.setSocketAddress(address);
		
		//		try {
		//			packet = new DatagramPacket(b, off, len, address);
		//		} catch (SocketException e) {
		//			Logger.error(e);
		//		}
		
		// 发送数据包
		boolean success = (packet != null);
		if (success) {
			success = replyTo(packet);
		}
		return success;
	}

}