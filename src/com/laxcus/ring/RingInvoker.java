/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ring;

import java.io.*;

import com.laxcus.command.mix.*;
import com.laxcus.fixp.client.*;
import com.laxcus.log.client.*;
import com.laxcus.util.net.*;

/**
 * RINT命令处理器
 * 
 * @author scott.liang
 * @version 1.0 9/30/2019
 * @since laxcus 1.0
 */
public class RingInvoker implements Runnable {
	
	/** 输入器 **/
	private RingInputter inputter;
	
	/** 设置编号 **/
	private int no;
	
	/** RING命令 **/
	private Ring cmd;

	/** 线程句柄 */
	private Thread thread;

	/** 线程运行标记 */
	private volatile boolean running;

	/**
	 * 构造RINT命令处理器
	 * @param inputter 输入器
	 * @param cmd 命令
	 * @param id 编号
	 */
	public RingInvoker(RingInputter inputter, Ring cmd, int id) {
		super();
		setInputter(inputter);
		setRing(cmd);
		setNO(id);
	}

	/**
	 * 设置输入器
	 * @param e
	 */
	public void setInputter(RingInputter e) {
		inputter = e;
	}

	/**
	 * 设置命令
	 * @param e
	 */
	public void setRing(Ring e) {
		cmd = e;
	}

	/**
	 * 设置编号
	 * @param who 编号
	 */
	public void setNO(int who) {
		no = who;
	}

	/**
	 * 返回编号
	 * @return 编号
	 */
	public int getNO() {
		return no;
	}
	
	/**
	 * 线程延时等待。单位：毫秒。
	 * @param ms 超时时间
	 */
	private synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				wait(ms);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断线程处于运行状态
	 * @return 返回真或者假
	 */
	public final boolean isRunning() {
		return running && thread != null;
	}
	
	/**
	 * 判断线程处于停止状态
	 * @return 返回真或者假
	 */
	public boolean isStopped() {
		return !isRunning();
	}

	/**
	 * 启动线程，在启动线程前调用"init"方法
	 * @param priority 线程优化级，见Thread中的定义
	 * @return 成功返回“真”，失败“假”。
	 */
	public boolean start(int priority) {
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
	public boolean start() {
		return start(Thread.NORM_PRIORITY);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 判断TCP/UDP
		SocketHost remote = cmd.getRemote();
		if (remote.isStream()) {
			testStream(cmd);
		} else if (remote.isPacket()) {
			testPacket(cmd);
		}

		// 删除句柄
		inputter.remove(no);
	}
	
	/**
	 * 格式化时间
	 * @param time
	 * @return
	 */
	private String format(long time) {
		long suffix = time % 1000;
		// 前缀0
		String s = String.format("%d", suffix);
		while (s.length() < 3) {
			s = "0" + s;
		}
		long prefix = (time - suffix) / 1000;
		return String.format("%d.%s", prefix, s);
	}
	
	/**
	 * 打印结果
	 * @param remote
	 * @param local
	 * @param from
	 * @param all
	 * @param count
	 * @param usedTime
	 */
	private void print(String remote, SocketHost local, SocketHost from, int all, int count, long usedTime) {
		StringBuffer bf = new StringBuffer();
		
		String time = format(usedTime); // String.format("%.3f", (double) (usedTime / 1000));
		bf.append(String.format("run time: %s seconds\n", time));
		
		// 服务器主机/本地主机/NAT主机
		bf.append(String.format("server host: %s\n", (remote != null ? remote : "null")));
		bf.append(String.format("local host: %s\n", (local != null ? local.toString() : "null")));
		bf.append(String.format("detect address: %s\n", (from != null ? from.toString() : "null")));
		
		// 判断是NAT网络
		if (local != null && from != null) {
			boolean nat = !Address.contains(from.getAddress());
			String str = String.format("nat network: %s\n", (nat ? "Yes" : "No"));
			bf.append(str);
		}

		bf.append(String.format("successful: %d\n", count));
		bf.append(String.format("failed: %d\n", all - count));
		double fs = ((double) count / (double) all) * 100.0f;
		String s = String.format("%.0f", fs);
		bf.append("successful rate: " + s + "%\n");
		// 打印结果
		System.out.print(bf.toString());
	}
	/**
	 * 关闭SOCKET
	 */
	private void close(FixpStreamClient client) {
		client.close();
	}
	
	/**
	 * 测试FIXP TCP通信
	 * @param cmd
	 */
	private void testStream(Ring cmd) {
		SocketHost remote = cmd.getRemote();
		int all = cmd.getCount();
		int count = 0;
		
		// 本地地址/出口地址（NAT出口）
		SocketHost local = null;
		SocketHost from = null;
		int timeout = cmd.getSocketTimeout();
		
		// 流客户端
		FixpStreamClient client = new FixpStreamClient();
		long time = System.currentTimeMillis();

		// 连接
		for (int i = 0; i < all; i++) {
			if (i > 0 && cmd.getDelay() > 0) {
				delay(cmd.getDelay());
			}
			
			// 连接
			if (!client.isConnected()) {
				client.setReceiveTimeout(timeout);
				client.setConnectTimeout(timeout);
				// 连接
				try {
					client.connect(remote);
				} catch (IOException e) {
					close(client);
					Logger.error(e);
					continue;
				}
			}

			// 通信
			try {
				// 发送数据包
				SocketHost host = client.test();
				// 判断成功
				boolean success = (host != null);
				if (success) {
					local = client.getLocal();
					from = host;
					count++;
				}
			} catch (IOException e) {
				close(client);
				Logger.error(e);
			} catch (Throwable e) {
				close(client);
				Logger.fatal(e);
			}
		}
		
		// 关闭socket
		close(client);
		
		long usedTime = System.currentTimeMillis() - time;

		print(cmd.getSocket(), local, from, all, count, usedTime);
	}

	/**
	 * 测试FIXP UDP通信
	 * @param cmd RING命令
	 */
	private void testPacket(Ring cmd) {
		SocketHost remote = cmd.getRemote();
		boolean secure = cmd.isSecure(); // 安全模式，或者否

		int all = cmd.getCount();
		int count = 0;
		
		int timeout = cmd.getSocketTimeout();
		FixpPacketClient client = new FixpPacketClient();
		client.setConnectTimeout(timeout);
		client.setReceiveTimeout(timeout);
		client.setSubPacketTimeout(timeout);
	
		long time = System.currentTimeMillis();
		
		// 判断本地IP
		boolean success = false;
		try {
			success = client.bind();
		} catch (IOException e) {
			Logger.error(e);
		}
		
		// 本机地址/出口地址（NAT出口）
		SocketHost local = null;
		SocketHost from = null;

		// 如果成功，发送数据包
		if (success) {
			local = client.getLocal();
			// 循环发包
			for (int i = 0; i < all; i++) {
				if (i > 0 && cmd.getDelay() > 0) {
					delay(cmd.getDelay());
				}
				try {
					SocketHost host = client.test(remote, secure);
					success = (host != null);
					if (success) {
						from = host;
						count++;
					}
				} catch (IOException e) {
					Logger.error(e);
				} catch (Throwable e) {
					Logger.fatal(e);
				}
			}
		}
		// 关闭UDP连接
		client.close();
		
		long usedTime = System.currentTimeMillis() - time;

		print(cmd.getSocket(), local, from, all, count, usedTime);
	}

}

///**
// * 测试FIXP UDP通信
// * @param cmd RING命令
// */
//private void testPacket(Ring cmd) {
//	SocketHost remote = cmd.getRemote();
//	boolean secure = cmd.isSecure(); // 安全模式，或者否
//	boolean exit = secure; // 如果启动加密通信，在关闭socket前就要启动“exit”退出操作。
//
//	int all = cmd.getCount();
//	int count = 0;
//	FixpPacketClient client = new FixpPacketClient();
//	client.setReceiveTimeout(cmd.getSocketTimeout());
//	
//	// 判断本地IP
//	boolean success = false;
//	try {
//		success = client.bind();
//	} catch (IOException e) {
//		Logger.error(e);
//	}
//
//	// 如果成功，发送数据包
//	if (success) {
//		for (int i = 0; i < all; i++) {
//			if (i > 0 && cmd.getDelay() > 0) {
//				delay(cmd.getDelay());
//			}
//			try {
//				success = client.test(remote, secure);
//				if (success) count++;
//			} catch (IOException e) {
//				Logger.error(e);
//			}
//		}
//	}
//	// 关闭UDP连接
//	client.close(exit);
//
//	print(cmd.getSocket(), all, count);
//}
