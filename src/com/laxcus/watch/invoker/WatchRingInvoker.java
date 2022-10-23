/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.awt.*;
import java.io.*;

import com.laxcus.command.mix.*;
import com.laxcus.fixp.client.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.net.*;

/**
 * 站点连接测试调用器
 * @author scott.liang
 * @version 1.0 2/1/2019
 * @since laxcus 1.0
 */
public class WatchRingInvoker extends WatchInvoker {

	/**
	 * 构造站点连接测试调用器，指定命令
	 * @param cmd 站点连接测试命令
	 */
	public WatchRingInvoker(Ring cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Ring getCommand() {
		return (Ring) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Ring cmd = getCommand();
		SocketHost remote = cmd.getRemote();
		if (remote.isStream()) {
			testStream(cmd);
		} else if (remote.isPacket()) {
			testPacket(cmd);
		}
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 格式化时间
	 * @param time
	 * @return
	 */
	private void formatTime(long time) {
		long suffix = time % 1000;
		// 前缀0
		String s = String.format("%d", suffix);
		while (s.length() < 3) {
			s = "0" + s;
		}
		long prefix = (time - suffix) / 1000;
		String str = String.format("%d.%s", prefix, s);

		String txtTime = getXMLContent("RING/TIME");
		Color foreground = findXMLForeground("RING/TIME");
		
		ShowItem showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, txtTime, foreground));
		showItem.add(new ShowStringCell(1, str));
		addShowItem(showItem);
	}
	
	
	/**
	 * 打印结果
	 * @param remote 目标节点
	 * @param all 命令发送次数
	 * @param count 成功统计
	 */
	private void print(long usedTime, String remote, SocketHost local, SocketHost from, int all, int count) {
		// 打印消耗的时间
		printRuntime();

		// 标题
		createShowTitle(new String[] { "RING/T1", "RING/T2" });
		
		String txtSocket = getXMLContent("RING/SERVER");
		Color colorSocket = findXMLForeground("RING/SERVER");
		
		String txtLocal = getXMLContent("RING/LOCAL");
		Color colorLocal = findXMLForeground("RING/LOCAL");
		
		String txtFrom = getXMLContent("RING/FROM");
		Color colorFrom = findXMLForeground("RING/FROM");
		
		String txtNAT = getXMLContent("RING/NAT");
		Color colorNAT = findXMLForeground("RING/NAT");

		String txtSuccess = getXMLContent("RING/SUCCESS");
		Color colorSuccess = findXMLForeground("RING/SUCCESS");

		String txtFail = getXMLContent("RING/FAIL");
		Color colorFail = findXMLForeground("RING/FAIL");

		String txtRatio = getXMLContent("RING/RATIO");
		Color colorRatio = findXMLForeground("RING/RATIO");
		
		// 显示时间
		formatTime(usedTime);

		// 地址
		ShowItem showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, txtSocket, colorSocket));
		showItem.add(new ShowStringCell(1, remote));
		addShowItem(showItem);

		// 本机地址
		showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, txtLocal, colorLocal));
		showItem.add(new ShowStringCell(1, local));
		addShowItem(showItem);
		
		// 本机出口地址(NAT)
		showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, txtFrom, colorFrom));
		showItem.add(new ShowStringCell(1, from));
		addShowItem(showItem);
		
		// 判断是NAT
		if (local != null && from != null) {
			boolean nat = !Address.contains(from.getAddress()); // 本地不存在这个地址，是NAT网络
			showItem = new ShowItem();
			showItem.add(new ShowStringCell(0, txtNAT, colorNAT));
			showItem.add(new ShowStringCell(1, (nat ? "Yes" : "No")));
			addShowItem(showItem);
		}
		
		// 成功
		showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, txtSuccess, colorSuccess));
		showItem.add(new ShowIntegerCell(1, count));
		addShowItem(showItem);

		// 失败
		showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, txtFail, colorFail));
		showItem.add(new ShowIntegerCell(1, all - count));
		addShowItem(showItem);

		// 成功比例
		double fs = ((double)count / (double) all) * 100.0f;
		String s = String.format("%.0f", fs);
		showItem = new ShowItem();
		showItem.add(new ShowStringCell(0, txtRatio, colorRatio));
		showItem.add(new ShowStringCell(1, s + "%"));
		addShowItem(showItem);
		
		// 输出全部记录
		flushTable();
	}

//	/**
//	 * 测试FIXP TCP通信
//	 * @param cmd
//	 */
//	private void testStream(Ring cmd) {
//		SocketHost remote = cmd.getRemote();
//		int all = cmd.getCount();
//		int count = 0;
//		
//		// 本地地址/出口地址（NAT出口）
//		SocketHost local = null;
//		SocketHost from = null;
//		int timeout = cmd.getSocketTimeout();
//
//		// 连接
//		for (int i = 0; i < all; i++) {
//			if (i > 0 && cmd.getDelay() > 0) {
//				delay(cmd.getDelay());
//			}
//
//			// 通信
//			FixpStreamClient client = new FixpStreamClient();
//			try {
//				client.setReceiveTimeout(timeout);
//				client.setConnectTimeout(timeout);
//				// 连接
//				client.connect(remote);
//				// 发送数据包
//				SocketHost host = client.test();
//				// 判断成功
//				boolean success = (host != null);
//				if (success) {
//					local = client.getLocal();
//					from = host;
//					count++;
//				}
//			} catch (IOException e) {
//				Logger.error(e);
//			} catch (Throwable e) {
//				Logger.fatal(e);
//			}
//			// 关闭socket
//			client.close();
//		}
//
//		print(cmd.getSocket(), local, from, all, count);
//	}
	
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
					Logger.error(e);
					close(client);
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

		print(usedTime, cmd.getSocket(), local, from, all, count);
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
		
		int timeout= cmd.getSocketTimeout();
		FixpPacketClient client = new FixpPacketClient();
		client.setConnectTimeout(timeout);
		client.setReceiveTimeout(timeout);
		client.setSubPacketTimeout(timeout);
		
		long time = System.currentTimeMillis();
		
		// 判断本地IP，注意！只是绑定本地IP，不是连接到服务器。
		// 绑定IP可以接收多个地址反馈的数据包！Reply服务端现在用Massive MIMO通信，会有ReplySucker和多个MISucker服务端地址发送反馈结果！
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
			// 循环
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
				}
			}
		}
		// 关闭UDP连接
		client.close();
		
		long usedTime = System.currentTimeMillis() - time;

		print(usedTime, cmd.getSocket(), local, from, all, count);
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
//	// 判断本地IP，注意！只是绑定本地IP，不是连接到服务器。
//	// 绑定IP可以接收多个地址反馈的数据包！Reply服务端现在用Massive MIMO通信，会有ReplySucker和多个MISucker服务端地址发送反馈结果！
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
