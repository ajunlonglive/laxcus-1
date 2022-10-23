/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import java.io.*;
import java.net.*;
import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.security.*;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * FIXP数据包监听服务器。负责接收/发送数据包。<br>
 * 
 * @author scott.liang
 * @version 1.3 5/16/2014
 * @since laxcus 1.0
 */
public class FixpPacketMonitor extends FixpMonitor implements PacketTransmitter, PacketMessenger {

	/** FIXP监听套接字 */
	private DatagramSocket server;

	/** UDP SOCKET发送缓冲区，默认1M */
	private int sendBufferSize;

	/** 密文存储器 **/
	private SecureCollector secureCollector;

	/** 安全委托器 **/
	private SecureTrustor secureTrustor;

	/** FIXP数据包协处理器 **/
	private FixpPacketHelper packetHelper;

	/**
	 * 构造FIXP数据包服务器，数据包并行处理线程数量
	 */
	public FixpPacketMonitor() {
		super();
		// 接收/发送缓存默认5M
		setReceiveBufferSize(0x500000);
		setSendBufferSize(0x500000);

		secureCollector = new SecureCollector();
		secureTrustor = new SecureTrustor(this, secureCollector);
		packetHelper = new FixpPacketHelper(this, secureCollector);
	}

	/**
	 * 设置处理UDP数据业务的线程数目
	 * @param num 线程数目
	 */
	public void setTaskThreads(int num) {
		packetHelper.setTaskThreads(num);
	}

	/**
	 * 返回处理UDP数据业务的线程数目
	 * @return 线程数目
	 */
	public int getTaskThreads() {
		return packetHelper.getTaskThreads();
	}

	/**
	 * 绑定密钥存储器。5秒钟间隔触发一次检查
	 * @param timer 计时器
	 */
	public void attachSecureCollector(Timer timer) {
		timer.schedule(secureCollector, 0, 5000);
	}

	/**
	 * 返回FIXP数据包协处理器
	 * @return FixpPacketHelper实例
	 */
	public FixpPacketHelper getPacketHelper() {
		return packetHelper;
	}

	/**
	 * 设置RPC调用接口
	 * @param e VisitInvoker实例
	 */
	public void setVisitInvoker(VisitInvoker e) {
		packetHelper.setVisitInvoker(e);
	}

	/**
	 * 返回RPC调用接口
	 * @return VisitInvoker实例
	 */
	public VisitInvoker getVisitInvoker() {
		return packetHelper.getVisitInvoker();
	}

	/**
	 * 设置FIXP数据包分派接口
	 * @param e PacketInvoker实例
	 */
	public void setPacketInvoker(PacketInvoker e) {
		packetHelper.setPacketInvoker(e);
	}

	/**
	 * 返回FIXP数据包分派接口
	 * @return PacketInvoker实例
	 */
	public PacketInvoker getPacketInvoker() {
		return packetHelper.getPacketInvoker();
	}

	/**
	 * 保存密文
	 * @param endpoint 目标站点
	 * @param cipher 密文
	 * @return 成功返回真，否则假
	 */
	public boolean addCipher(SocketHost endpoint, Cipher cipher) {
		return secureCollector.add(endpoint, cipher);
	}

	/**
	 * 根据地址删除本地保存的密文
	 * @param endpoint
	 * @return 返回被删除的密钥
	 */
	public Cipher dropCipher(SocketHost endpoint) {
		return secureCollector.drop(endpoint);
	}
	
	/**
	 * 根据地址删除本地保存的密文
	 * @param endpoint
	 * @return 删除成功返回真，否则假。
	 */
	public boolean removeCipher(SocketHost endpoint) {
		return secureCollector.remove(endpoint);
	}

	/**
	 * 根据地址查找密文
	 * @param endpoint SOCKET地址
	 * @return 返回密文或者空
	 */
	public Cipher findCipher(SocketHost endpoint) {
		return secureCollector.find(endpoint);
	}

	/**
	 * 设置发送缓冲区尺寸
	 * @param size 缓冲区尺寸
	 * @return 返回新设置的缓存尺寸
	 */
	public int setSendBufferSize(int size) {
		if (size >= 0x100000) {
			sendBufferSize = size;
		}
		return sendBufferSize;
	}

	/**
	 * 返回发送缓冲区尺寸
	 * @return 缓冲区尺寸
	 */
	public int getSendBufferSize() {
		return sendBufferSize;
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.FixpMonitor#isBound()
	 */
	@Override
	public boolean isBound() {
		return server != null && server.isBound();
	}

	/**
	 * 关闭UDP套接字
	 */
	public void close() {
		// 关闭SOCKET
		try {
			if (server != null) {
				server.close();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			server = null;
		}
	}

	/**
	 * 绑定本地套接字地址。
	 * @param local 当前节点地址
	 * @return 成功返回真，失败返回假
	 */
	public boolean bind(SocketHost local) {
		boolean success = false;

		SocketAddress address = local.getSocketAddress();
		try {
			server = new DatagramSocket(null);
			server.bind(address);
			// 设置SOCKET的接收/发送缓存
			if (getReceiveBufferSize() > 0) {
				server.setReceiveBufferSize(getReceiveBufferSize());
				
				Logger.debug(this, "bind", "set receive buff:%s, real receive buff:%s",
						ConfigParser.splitCapacity(getReceiveBufferSize()),
						ConfigParser.splitCapacity(server.getReceiveBufferSize()));
			}
			if (getSendBufferSize() > 0) {
				server.setSendBufferSize(getSendBufferSize());

				Logger.debug(this, "bind", "set send buff:%s, real send buff:%s",
						ConfigParser.splitCapacity(getSendBufferSize()),
						ConfigParser.splitCapacity(server.getSendBufferSize()));
			}
			// 无限制等待数据包
			server.setSoTimeout(0); // no limit time
			success = true;
		} catch (SocketException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		if (success) {
			// SOCKET发送/接收缓存尺寸
			try {
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
				
				// 记录它的缓存尺寸
				EchoTransfer.setCommandPacketBuffer(readSize, writeSize);
			} catch (IOException e) {
				Logger.error(e);
			}
			Logger.info(this, "bind", "real bind: %s", getBindHost());
		}

		return success;
	}

	/**
	 * 根据应答码生成一个错误数据包
	 * @param reply 应答码
	 * @return FIXP数据包
	 */
	protected Packet invalid(short reply) {
		Mark cmd = new Mark(reply);
		Packet packet = new Packet(cmd);
		packet.addMessage(MessageKey.SPEAK, "sorry!");
		return packet;
	}

	/**
	 * 向目标地址发送UDP包。<br>
	 * 注意：这里发送的是UDP包！！！所有UDP通讯最后都是通过这个方法发送出去。
	 * 
	 * @param remote 目标地址
	 * @param b FIXP数据包字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @param count 发送次数
	 * @return 发送成功返回真，否则假。
	 */
	protected int sendTo(SocketHost remote, byte[] b, int off, int len, final int count) {
		int total = 0;

		// Logger.debug(this, "sendTo", "投递数据包到 %s ，数据尺寸：%d", remote, len);

		// 客户机目标地址
		InetSocketAddress address = remote.getSocketAddress();
		DatagramPacket datagram = new DatagramPacket(b, off, len);
		datagram.setSocketAddress(address);

		try {
			// UDP SOCKET有效时才处理
			if (server != null) {
				for (int i = 0; i < count; i++) {
					server.send(datagram);
					total++;
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 返回发送的包数目
		return total;
	}
	
	/**
	 * 向目标地址发送UDP包。<br>
	 * 注意：这里发送的是UDP包！！！所有UDP通讯最后都是通过这个方法发送出去。
	 * 
	 * @param remote 目标地址
	 * @param b FIXP数据包字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 发送成功返回真，否则假。
	 */
	private boolean sendTo(SocketHost remote, byte[] b, int off, int len) {
		int count = sendTo(remote, b, off, len, 1);
		return (count > 0);
	}

	/**
	 * 以顺序的方式，向请求方返回应答UDP包。如果有加密要求先做加密处理。
	 * 
	 * @param remote 目标地址
	 * @param b FIXP数据包字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 发送成功返回真，否则假
	 */
	protected int send(SocketHost remote, byte[] b, int off, int len, final int count) {
		// 如果有密文时，加密发送
		try {
			Cipher cipher = secureCollector.find(remote);
			// 密文存在，加密发送
			if (cipher != null) {
				b = cipher.encrypt(b, off, len);
				// 重置
				off = 0; len = b.length;
			}
		} catch (SecureException e) {
			Logger.error(e);
			return 0;
		}
		// 发送数据报文
		return sendTo(remote, b, off, len, count);
	}

	/**
	 * 以锁定且顺序的方式，向请求方返回应答UDP包。如果有加密要求先做加密处理。
	 * 
	 * @param remote 目标地址
	 * @param b FIXP数据包字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 发送成功返回真，否则假
	 */
	protected boolean send(SocketHost remote, byte[] b, int off, int len) {
		int count = send(remote, b, off, len, 1);
		return (count > 0);
	}

	/**
	 * 指定客户机目标地址，向客户机发送FIXP数据包。
	 * 
	 * @param remote 客户机目标地址
	 * @param packet FIXP数据包
	 * @param count 发送次数
	 * @return 发送成功返回真，否则假
	 */
	protected int send(SocketHost remote, Packet packet, final int count) {
		// 检查目标地址
		if (!remote.isValid()) {
			throw new IllegalArgumentException("illegal address!");
		}
		// 生成字节流，发送数据
		byte[] b = packet.build();
		return send(remote, b, 0, b.length, count);
	}

	/**
	 * 指定客户机目标地址，向客户机发送FIXP数据包。
	 * 
	 * @param remote 客户机目标地址
	 * @param packet FIXP数据包
	 * @return 发送成功返回真，否则假
	 */
	protected boolean send(SocketHost remote, Packet packet) {
		int count = send(remote, packet, 1);
		return (count > 0);
	}

	/**
	 * 调用者通过数据包转发器，借助FIXP UDP服务器，直接发送应答数据包。
	 * @see com.laxcus.invoke.PacketTransmitter#reply(com.laxcus.fixp.Packet)
	 */
	@Override
	public boolean reply(Packet resp) {
		// 必须是应答包
		if (!resp.getMark().isAnswer()) {
			throw new IllegalArgumentException("illegal packet!");
		}
		SocketHost remote = resp.getRemote();
		if (!remote.isValid()) {
			throw new IllegalArgumentException("illegal remote address!");
		}
		// 发送字节流
		byte[] b = resp.build();
		return send(remote, b, 0, b.length);
	}

	/**
	 * 站点以客户端身份，向其它站点发送请求包，包括对加密的请求
	 * @param request 请求包
	 * @return 成功返回真，否则假
	 */
	protected boolean __notice(Packet request) {
		// 目标地址
		SocketHost remote = request.getRemote();

		// 判断密文存在
		boolean encrypted = secureCollector.hasCipher(remote);

//		Logger.debug(this, "__notice", "%s %s %s", 
//				(encrypted ? "encrypted" : "unencrypted"), remote, request.getMark());

		// 如果定义密钥，走“send”方法加密通道，然后发送
		if (encrypted) {
			return send(remote, request);
		}
		
		// 没有定义密钥时，由安全委托器代理检查服务器站点属性后，再调用FixpPacketMonitor.__notice方法发送！
		ClientSecure secure = secureTrustor.findSiteSecure(remote);
		// 不确定(==null)或者服务器要求加密时，交给安全代理暂存并处理
		if (secure == null || SecureType.isCipher(secure.getFamily())) {
//			int family = (secure != null ? secure.getFamily() : SecureType.INVALID);
//			Logger.debug(this, "__notice", "%s Server Secure Type: %d -> %s", remote, family, SecureType.translate(family));

			return secureTrustor.addPacket(request);
		} else {
//			Logger.debug(this, "__notice", "direct to %s", remote);
			// 没有加密要求，直接发送
			byte[] b = request.build();
			return sendTo(remote, b, 0, b.length);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.PacketMessenger#notice(com.laxcus.fixp.Packet)
	 */
	@Override
	public boolean notice(Packet request) {
		// 必须是请求包
		if (!request.getMark().isAsk()) {
			throw new IllegalValueException("illegal packet!");
		}
		// 必须携带目标地址
		SocketHost remote = request.getRemote();
		if (!remote.isValid()) {
			throw new IllegalValueException("illegal site host! %s", remote);
		}

		// 保存到队列中
		return __notice(request);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 启动前必须绑定
		if (!isBound()) {
			Logger.error(this, "init", "must be bind socket!");
			return false;
		}

		// 1. 启动安全代理器
		boolean success = secureTrustor.start();
		// 2. 启动数据包辅助处理器
		if (success) {
			success = packetHelper.start();
			if (!success) {
				secureTrustor.stop();
			}
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 关闭监听套接字
		close();
		// 停止数据包辅助处理器并且等待完成
		ThreadStick stick = new ThreadStick();
		packetHelper.stop(stick);
		while (!stick.isOkay()) {
			delay(200);
		}
		// 停止安全代理
		secureTrustor.stop();

		Logger.info(this, "finish", "finished!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		// UDP最大尺寸: 0xffff - 20(ip head) - 8(udp head)，即65507字节。
		byte[] buff = new byte[ReplyTransfer.MTU]; 
		DatagramPacket udp = new DatagramPacket(buff, buff.length);

		while (!isInterrupted()) {			
			try {
				// 接收UDP数据包
				server.receive(udp);
				// 分配UDP数据包
				distribute(udp);
			} catch (IOException e) {
				if (isInterrupted()) {
					break;
				}
				// 打印日志 
				Logger.error(e);
			} catch (Throwable e) {
				if (isInterrupted()) {
					break;
				}
				// 打印日志
				Logger.fatal(e);
			}
		}

		// 释放
		udp = null;
		buff = null;

		Logger.info(this, "process", "exit...");
	}

	/**
	 * 检查数据报文，分配给辅助器处理
	 * @param datagram UDP数据报文
	 */
	private final void distribute(DatagramPacket datagram) {
		// 数据包的来源地址
		SocketHost from = new SocketHost(SocketTag.UDP, datagram.getAddress(), datagram.getPort());

		// 根据客户机来源的IP地址，找到对应这个IP地址的服务器密钥令牌
		Address address = from.getAddress();
		// 判断接受这个IP地址
		boolean success = SecureController.getInstance().allow(address);
		if (!success) {
			Logger.error(this, "distribute", "refuse %s", from);
			return;
		}

		// 检查数据包长度
		if (datagram.getLength() < 1) {
			Logger.error(this, "distribute", "empty datagram! from %s", from);
			return;
		}

		// 保存参数
		packetHelper.add(from, datagram.getData(), datagram.getOffset(), datagram.getLength());
	}
	
	/**
	 * 返回本地绑定地址
	 * @return Address实例
	 */
	public Address getBindAddress() {
		if (server == null || server.isClosed()) {
			return null;
		}
		return new Address(server.getLocalAddress());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.FixpMonitor#getBindHost()
	 */
	@Override
	public SocketHost getBindHost() {
		if (server == null || server.isClosed()) {
			return null;
		}
		SocketHost host = new SocketHost(SocketTag.UDP, server.getLocalAddress(),
				server.getLocalPort());
		host.setReflectPort(getReflectPort());
		return host;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.FixpMonitor#getBindPort()
	 */
	@Override
	public int getBindPort() {
		if (server == null || server.isClosed()) {
			return 0;
		}
		return server.getLocalPort();
	}

	/**
	 * 返回网关检测的内网在NAT的出口地址
	 * @param remote 网关站点地址
	 * @return SocketHost实例，或者空指针
	 */
	public SocketHost findPockLocal(SocketHost remote) {
		return packetHelper.findPockLocal(remote);
	}

	/**
	 * 判断有NAT出口地址
	 * @return 返回真或者假
	 */
	public boolean hasPocks() {
		return packetHelper.hasPocks();
	}

	/**
	 * 重置，当重新注册时
	 */
	protected void reset() {
		secureCollector.clear();
		secureTrustor.clear();
	}

}


//	/**
//	 * 重装绑定指定的本地套接字地址
//	 * @return 绑定成功返回真，否则假
//	 */
//	private boolean rebind() {
//		boolean success = false;
//		while (!isInterrupted()) {
//			close();
//			delay(1000); // 延时1秒，重新绑定
//			SocketHost host = getLocal();
//			success = bind(host);
//			if (success) break;
//		}
//		return success;
//	}

///**
// * 检查数据报文，分配给辅助器处理
// * @param datagram UDP数据报文
// */
//private final void distribute(DatagramPacket datagram) {
//	// 数据包的来源地址
//	SocketHost from = new SocketHost(SocketTag.UDP, datagram.getAddress(), datagram.getPort());
//
//	// 根据客户机来源的IP地址，找到对应这个IP地址的服务器密钥令牌
//	Address address = from.getAddress();
//	ServerToken token = ServerTokenManager.getInstance().find(address);
//	// 如果没有找到服务器令牌，或者令牌中不包含这个IP地址，拒绝它的连接（这项限制可以防止DDOS攻击）
//	if (token == null || !token.contains(address)) {
//		Logger.error(this, "distribute", "refuse %s", from);
//		return;
//	}
//
//	// 检查数据包长度
//	if (datagram.getLength() < 1) {
//		Logger.error(this, "distribute", "empty datagram! from %s", from);
//		return;
//	}
//
//	// 保存参数
//	packetHelper.add(from, datagram.getData(), datagram.getOffset(), datagram.getLength());
//}


///**
// * 向目标地址发送UDP包。<br>
// * 注意：这里发送的是UDP包！！！所有UDP通讯最后都是通过这个方法发送出去。
// * 
// * @param remote 目标地址
// * @param b FIXP数据包字节数组
// * @param off 开始下标
// * @param len 有效长度
// * @param count 发送次数
// * @return 发送成功返回真，否则假。
// */
//protected int sendTo(SocketHost remote, byte[] b, int off, int len, final int count) {
//	int total = 0;
//
//	// Logger.debug(this, "sendTo", "投递数据包到 %s ，数据尺寸：%d", remote, len);
//
//	// 客户机目标地址
//	InetSocketAddress address = remote.getSocketAddress();
//	DatagramPacket udp = new DatagramPacket(b, off, len);
//	udp.setSocketAddress(address);
//
//	// 锁定发送FIXP数据包，减少并行发送量，提高发送成功率
//	super.lockSingle();
//	try {
//		// UDP SOCKET有效时才处理
//		if (server != null) {
//			for (int i = 0; i < count; i++) {
//				server.send(udp);
//				total++;
//			}
//		}
//	} catch (IOException e) {
//		Logger.error(e);
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//
//	// 返回发送的包数目
//	return total;
//}


///**
// * 建立密文通信<br>
// * 
// * 这个FIXP服务器使用自己的信道，以客户机的身份，向真实的FIXP服务器，投递RSA密文。
// * 要求对方服务器保存这个RSA密文，做为此后UDP安全通信依据。<br>
// * 
// * 对应FixpPacketHelper.createSecure方法。<br>
// * 
// * @param remote 目标站点地址
// * @param cipher FIXP密文
// * @return 加密且发送成功返回真，或者假
// */
//protected boolean createSecure(SocketHost remote, Cipher cipher) {
//	// 用公钥对密文进行加密
//	byte[] data = null;
//	try {
//		Address address = remote.getAddress();
//		ClientToken token = ClientTokenManager.getInstance().find(address);
//		if (token == null) {
//			Logger.error(this, "createSecure",
//					"cannot be find client-token by '%s', please check local.xml", address);
//			return false;
//		}
//		// 对密文用RAS公钥加密
//		data = cipher.encase(token.getKey());
//	} catch (SecureException e) {
//		Logger.error(e);
//		return false;
//	}
//
//	// 建立加密初始化数据包
//	Mark cmd = new Mark(Ask.NOTIFY, Ask.SECURE_CREATE);
//	Packet packet = new Packet(remote, cmd);
//	packet.setData(data);
//	// 直接发送数据包
//	byte[] b = packet.build();
//
//	// 向目标站点发送加密初始化数据包
//	return sendTo(remote, b, 0, b.length);
//}

///**
// * 站点以客户端身份，向其它站点发送请求包，包括对加密的请求
// * @param request 请求包
// * @return 成功返回真，否则假
// */
//protected boolean __notice(Packet request) {
//	// 目标地址
//	SocketHost remote = request.getRemote();
//
//	// 判断密文存在
//	boolean encrypted = secureCollector.hasCipher(remote);
//
//	Logger.debug(this, "__notice", "%s %s %s", 
//			(encrypted ? "encrypted" : "unencrypted"), remote, request.getMark());
//
//	// 如果定义密钥，走“send”方法加密通道，然后发送
//	if (encrypted) {
//		return send(remote, request);
//	}
//
//	// 没有定义密钥时，由安全委托器代理检查服务器站点属性后，再调用FixpPacketMonitor.__notice方法发送！
//	int family = secureTrustor.findSiteSecure(remote);
//	// 不确定(-1)或者服务器要求加密时，交给安全代理暂存并处理
//	if (family == -1 || SecureType.isCipher(family)) {
//		Logger.debug(this, "__notice", "%s Server Secure Type: %d -> %s", 
//				remote, family, SecureType.translate(family));
//
//		return secureTrustor.addPacket(request);
//	} else {
//		// 没有加密要求，直接发送
//		byte[] b = request.build();
//		return sendTo(remote, b, 0, b.length);
//	}
//}

