/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.monitor;

import java.io.*;
import java.net.*;
import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;
import com.laxcus.thread.*;
import com.laxcus.tub.invoke.*;
import com.laxcus.util.net.*;

/**
 * 边缘数据流监听服务器
 * 
 * @author scott.liang
 * @version 1.0 10/10/2020
 * @since laxcus 1.0
 */
public class TubStreamMonitor extends TubMonitor {

	/** 套接字堆栈队列可接受的最多SOCKET数目 **/
	private int blocks;

	/** 服务器SOCKET句柄 **/
	private ServerSocket server;

	/** RPC调用接口 **/
	private TubVisitInvoker visitInvoker;
	
	/** 方法调用接口 **/
	private TubMethodInvoker methodInvoker; 

	/** 数据流任务线程句柄 **/
	private ArrayList<TubTask> tasks = new ArrayList<TubTask>(20);

	/**
	 * 构造一个默认的边缘数据流监听服务器
	 */
	public TubStreamMonitor() {
		super();
		// 默认队列有20个SOCKET
		setBlocks(20);
		// 缓存默认5M
		setReceiveBufferSize(0x500000);
	}
	
	/**
	 * 统计边缘任务
	 * @return 返回真或者假
	 */
	public int countTasks() {
		return tasks.size();
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
	 * @param e TubVisitInvoker实例
	 */
	public void setVisitInvoker(TubVisitInvoker e) {
		visitInvoker = e;
	}

	/**
	 * 返回RPC调用接口
	 * @return TubVisitInvoker实例
	 */
	public TubVisitInvoker getVisitInvoker() {
		return visitInvoker;
	}

	/**
	 * 设置边缘数据流处理接口
	 * @param e MethodInvoker实例
	 */
	public void setMethodInvoker(TubMethodInvoker e) {
		methodInvoker = e;
	}

	/**
	 * 返回边缘数据流处理接口
	 * @return MethodInvoker实例
	 */
	public TubMethodInvoker getMethodInvoker() {
		return methodInvoker;
	}

	/**
	 * 删除边缘任务
	 * @param e
	 * @return
	 */
	public boolean remove(TubTask e) {
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
		if (server == null) {
			return;
		}
		// 关闭服务器套接字
		try {
			server.close();
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			server = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.TubMonitor#isBound()
	 */
	@Override
	public boolean isBound() {
		return server != null && server.isBound();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.TubMonitor#getBindHost()
	 */
	@Override
	public SocketHost getBindHost() {
		if (server == null || server.isClosed()) {
			return null;
		}
		return new SocketHost(SocketTag.TCP, server.getInetAddress(),
				server.getLocalPort());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.TubMonitor#getBindPort()
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

	/**
	 * 增加边缘数据流处理和启动它
	 * @param task 边缘数据流处理
	 */
	private void add(TubTask task) {
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
			for (TubTask task : tasks) {
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

	/**
	 * 检查和分发SOCKET
	 * @param socket 套接字
	 */
	private final void distribute(Socket socket) {
		//		// 客户机地址
		//		SocketHost from = new SocketHost(SocketTag.TCP, socket.getInetAddress(), socket.getPort());

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

		// 建立边缘数据流任务
		TubTask task = null;
		try {
			task = new TubTask(socket, this, visitInvoker, methodInvoker);
		} catch (IOException e) {
			Logger.error(e);
		}
		// 有效，保存它！
		if (task != null) {
			add(task);
		}
	}
}