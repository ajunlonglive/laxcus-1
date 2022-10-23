/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import java.io.*;
import java.net.*;
import java.util.*;

import com.laxcus.fixp.secure.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.thread.*;
import com.laxcus.util.net.*;

/**
 * FIXP数据流监听服务器
 * 
 * @author scott.liang
 * @version 1.3 5/18/2013
 * @since laxcus 1.0
 */
public class FixpStreamMonitor extends FixpMonitor implements StreamTransmitter {

	/** 套接字堆栈队列可接受的最多SOCKET数目 **/
	private int blocks;

	/** 服务器SOCKET句柄 **/
	private ServerSocket server;

	/** RPC调用接口 **/
	private VisitInvoker visitInvoker;

	/** 数据流调用接口，所有数据流通信转发给它处理。**/
	private StreamInvoker streamInvoker;

	/** 数据流任务线程句柄 **/
	private ArrayList<StreamTask> tasks = new ArrayList<StreamTask>(20);

	/**
	 * 构造一个默认的FIXP数据流监听服务器
	 */
	public FixpStreamMonitor() {
		super();
		// 默认队列有100个SOCKET
		setBlocks(100);
		// 缓存默认5M
		setReceiveBufferSize(0x500000);
	}

	/**
	 * 设置套接字堆栈队列可接受的最多SOCKET数目
	 * @param i 最大SOCKET数目
	 */
	public void setBlocks(int i) {
		if (i > 0) blocks = i;
	}

	/**
	 * 返回套接字堆栈队列可接受的最多SOCKET数目
	 * @return 最大SOCKET数目
	 */
	public int getBlocks() {
		return blocks;
	}

	/**
	 * 设置RPC调用接口
	 * @param e VisitInvoker实例
	 */
	public void setVisitInvoker(VisitInvoker e) {
		visitInvoker = e;
	}

	/**
	 * 返回RPC调用接口
	 * @return VisitInvoker实例
	 */
	public VisitInvoker getVisitInvoker() {
		return visitInvoker;
	}

	/**
	 * 设置FIXP数据流处理接口
	 * @param e StreamInvoker实例
	 */
	public void setStreamInvoker(StreamInvoker e) {
		streamInvoker = e;
	}

	/**
	 * 返回FIXP数据流处理接口
	 * @return StreamInvoker实例
	 */
	public StreamInvoker getStreamInvoker() {
		return streamInvoker;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.StreamTransmitter#remove(com.laxcus.fixp.monitor.StreamTask)
	 */
	@Override
	public boolean remove(StreamTask e) {
		// 锁定
		if (e != null) {
			super.lockSingle();
			try {
				return tasks.remove(e);
			} catch (Throwable t) {
				Logger.fatal(t);
			} finally {
				super.unlockSingle();
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#stop()
	 */
	@Override
	public void stop() {
		super.stop();
		close();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#stop(com.laxcus.thread.ThreadInductor)
	 */
	@Override
	public void stop(ThreadStick stick) {
		super.stop(stick);
		close();
	}

	/**
	 * 关闭TCP监听
	 */
	public void close() {
		// 关闭服务器套接字
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			server = null;
		}
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
	 * 返回本地绑定的IP地址
	 * @return 返回Address实例
	 */
	public Address getBindAddress() {
		if (server == null || server.isClosed()) {
			return null;
		}
		return new Address(server.getInetAddress());
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
		SocketHost host = new SocketHost(SocketTag.TCP, server.getInetAddress(),
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
	 * 根据配置要求绑定一个本地地址，接受远程SOCKET请求
	 * @return 绑定成功返回真，否则假
	 */
	public boolean bind(SocketHost local) {
		boolean success = false;
		SocketAddress address = local.getSocketAddress();
		try {
			server = new ServerSocket();
			server.bind(address, blocks);
			// 设置接收缓存
			if (getReceiveBufferSize() > 0) {
				server.setReceiveBufferSize(getReceiveBufferSize());
			}
			
			// SOCKET接收/发送缓冲区尺寸
			int readSize = server.getReceiveBufferSize();
			EchoTransfer.setCommandStreamBuffer(readSize, 0);
			
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		if (success) {
			Logger.info(this, "bind", "real bind: %s", getBindHost());
		}

		Logger.note(this, "bind", success, "local listen %s", local);

		return success;
	}
	
//	/**
//	 * 重新绑定到本地地址
//	 * @return 返回真或者假
//	 */
//	private boolean rebind() {
//		boolean success = false;
//		// 循环，直到绑定成功，或者系统要求退出
//		while (!isInterrupted()) {
//			// 关闭SOCKET
//			close();
//			// 留出1秒钟，释放资源
//			delay(1000);
//			// 绑定本地地址
//			SocketHost local = getLocal();
//			success = bind(local);
//			// 绑定成功，退出
//			if (success) break;
//			// 不成功，延时两秒再试
//			delay(2000);
//		}
//		return success;
//	}

	/**
	 * 增加FIXP数据流处理和启动它
	 * @param task FIXP数据流处理
	 */
	private void add(StreamTask task) {
		boolean success = false;
		// 保存任务句柄
		super.lockSingle();
		try {
			tasks.add(task);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 成功，启动线程
		if (success) {
			task.start();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 关闭SOCKET
		close();
		// 停止全部数据流处理任务
		super.lockSingle();
		try {
			for (StreamTask task : tasks) {
				task.stop();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 等待全部完成
		while(!tasks.isEmpty()) {
			delay(200);
		}
		Logger.info(this, "finish", "okay!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		if (!isBound()) {
			Logger.error(this, "init", "must be bind socket!");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		while (!isInterrupted()) {
			try {
				// 接收一个SOCKET
				Socket socket = server.accept();
				// 分发
				distribute(socket);
			} catch (IOException e) {
				// 判断是退出
				if (isInterrupted()) {
					break;
				}
				Logger.error(e);
			} catch (Throwable e) {
				// 判断是退出
				if (isInterrupted()) {
					break;
				}
				Logger.fatal(e);
			}
		}

		Logger.info(this, "process", "exit...");
	}

//	/**
//	 * 检查和分发SOCKET
//	 * @param socket 套接字
//	 */
//	private final void distribute(Socket socket) {
//		// 客户机地址
//		SocketHost from = new SocketHost(SocketTag.TCP, socket.getInetAddress(), socket.getPort());
//
//		// 根据客户机的IP地址，找到这个IP地址对应的服务器密钥令牌
//		Address address = from.getAddress();
//		ServerToken token = ServerTokenManager.getInstance().find(address);
//		// 如果没有服务器令牌，或者令牌中不包含这个IP地址，拒绝它的连接（这项限制可以防止DDOS攻击）
//		if (token == null || !token.contains(address)) {
//			Logger.error(this, "distribute", "refuse %s", from);
//			try {
//				socket.close();
//			} catch (IOException e) {
//				Logger.error(e);
//			}
//			return;
//		}
//
//		// 建立FIXP数据流任务
//		StreamTask task = null;
//		try {
//			task = new StreamTask(socket, this, visitInvoker, streamInvoker);
//		} catch (IOException e) {
//			Logger.error(e);
//		}
//		// 有效，保存它！
//		if (task != null) {
//			add(task);
//		}
//	}
	
	/**
	 * 检查和分发SOCKET
	 * @param socket 套接字
	 */
	private final void distribute(Socket socket) {
		// 客户机地址
		SocketHost from = new SocketHost(SocketTag.TCP, socket.getInetAddress(), socket.getPort());

		// 根据客户机的IP地址，找到这个IP地址对应的服务器密钥令牌
		Address address = from.getAddress();
		// 服务器判断接受这个IP地址
		boolean success = SecureController.getInstance().allow(address);
		if (!success) {
			Logger.error(this, "distribute", "refuse %s", from);
			try {
				socket.close();
			} catch (IOException e) {
				Logger.error(e);
			}
			return;
		}

		// 建立FIXP数据流任务
		StreamTask task = null;
		try {
			task = new StreamTask(socket, this, visitInvoker, streamInvoker);
		} catch (IOException e) {
			Logger.error(e);
		}
		// 有效，保存它！
		if (task != null) {
			add(task);
		}
	}
}