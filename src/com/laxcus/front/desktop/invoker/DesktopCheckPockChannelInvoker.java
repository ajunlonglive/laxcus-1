/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.awt.*;

import com.laxcus.command.cyber.*;
import com.laxcus.fixp.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.net.*;

/**
 * 检测服务器系统信息调用器。
 * 只在本地执行
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopCheckPockChannelInvoker extends DesktopInvoker {
	
	/** 控制、接收、发送信道 **/
	final static int CONTROL = 1;

	final static int SUCK = 2;

	final static int DISPATCH = 3;

	/**
	 * 构造应答包传输模式调用器，指定命令
	 * @param cmd 应答包传输模式
	 */
	public DesktopCheckPockChannelInvoker(CheckPockChannel cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckPockChannel getCommand() {
		return (CheckPockChannel) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		check();
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

//	private void test()  {
//		try {
//			Node hub = new Node(SiteTag.ENTRANCE_SITE, new SiteHost("12.89.23.33", 920, 877));
//			SocketHost remote = new SocketHost(SocketTag.UDP, "28.3.2.78", 766);
//			SocketHost local = new SocketHost(SocketTag.UDP, "12.33.4.44", 1233);
//
//			PockItem item = new PockItem(hub, remote ,local);
//			print(false, item);
//			print(true, item);
//		} catch (Exception e) {
//			Logger.error(e);
//		}
//	}

	/**
	 * 检测接口
	 */
	private void check() {
//		// 设置标题
//		createShowTitle(new String[] { "CHECK-POCK-CHANNEL/TYPE", "CHECK-POCK-CHANNEL/SERVER", "CHECK-POCK-CHANNEL/POCK", "CHECK-POCK-CHANNEL/BOUND" });

		// 设置标题
		createShowTitle(new String[] { "CHECK-POCK-CHANNEL/TYPE",
				"CHECK-POCK-CHANNEL/LOCAL", "CHECK-POCK-CHANNEL/POCK",
				"CHECK-POCK-CHANNEL/SERVER", "CHECK-POCK-CHANNEL/BOUND" });

		checkControlPock();
		checkDataPock();

//		test();

		// 输出全部
		flushTable();
	}

//	/**
//	 * 打印结果
//	 * @param control 控制信道
//	 * @param item
//	 */
//	private void print(boolean control, PockItem item) {
//		ShowItem showItem = new ShowItem();
//
//		// 类型
//		if (control) {
//			Color color = findXMLForeground("CHECK-POCK-CHANNEL/TYPE/CONTROL");
//			String name = getXMLContent("CHECK-POCK-CHANNEL/TYPE/CONTROL");
//			showItem.add(new ShowStringCell(0, name, color));
//		} else {
//			Color color = findXMLForeground("CHECK-POCK-CHANNEL/TYPE/DATA");
//			String name = getXMLContent("CHECK-POCK-CHANNEL/TYPE/DATA");
//			showItem.add(new ShowStringCell(0, name, color));
//		}
//
//		// 服务器
//		showItem.add(new ShowStringCell(1, item.getRemote()));
//		// NAT转义地址
//		showItem.add(new ShowStringCell(2, item.getLocalNAT()));
//		// 节点主机
//		if (item.getHub() != null) {
//			showItem.add(new ShowStringCell(3, item.getHub()));
//		} else {
//			showItem.add(new ShowStringCell(3, ""));
//		}
//
//		addShowItem(showItem);
//	}

//	/**
//	 * 显示控制信道
//	 */
//	private void checkControlPock() {
//		SocketHost local = getLauncher().getPacketHost();
//		FixpPacketHelper helper = getLauncher().getPacketHelper();
//
//		java.util.List<PockItem> array = getLauncher().getPacketHelper()
//		.getPocks();
//
//		for(PockItem item : array) {
//			print(true, local, item);
//		}
//	}
	
//	/**
//	 * 打印结果
//	 * @param control 控制信道
//	 * @param item
//	 */
//	private void print(boolean control, SocketHost local, PockItem item) {
//		ShowItem showItem = new ShowItem();
//
//		//1. 类型
//		if (control) {
//			Color color = findXMLForeground("CHECK-POCK-CHANNEL/TYPE/CONTROL");
//			String name = getXMLContent("CHECK-POCK-CHANNEL/TYPE/CONTROL");
//			showItem.add(new ShowStringCell(0, name, color));
//		} else {
//			Color color = findXMLForeground("CHECK-POCK-CHANNEL/TYPE/DATA");
//			String name = getXMLContent("CHECK-POCK-CHANNEL/TYPE/DATA");
//			showItem.add(new ShowStringCell(0, name, color));
//		}
//		
//		//2. 本地地址
//		showItem.add(new ShowStringCell(1, local.toString()));
//		// 3. NAT转义地址
//		showItem.add(new ShowStringCell(2, item.getLocalNAT()));
//		// 4.服务器
//		showItem.add(new ShowStringCell(3, item.getRemote()));
//
//		// 5. 节点主机
//		if (item.getHub() != null) {
//			showItem.add(new ShowStringCell(4, item.getHub()));
//		} else {
//			showItem.add(new ShowStringCell(4, ""));
//		}
//
//		addShowItem(showItem);
//	}
//
//	/**
//	 * 显示数据信道
//	 */
//	private void checkDataPock() {
//		SocketHost local = getLauncher().getSuckerBindHost(); // 真实地址
//		ReplyHelper helper = getLauncher().getReplyHelper();
//		PockItem[] array = helper.getPockItems();
//		for (PockItem item : array) {
//			print(false, local, item);
//		}
//	}
//
//	/**
//	 * 显示控制信道
//	 */
//	private void checkControlPock() {
//		SocketHost local = getLauncher().getPacketBindHost(); // 本地真实地址
//		FixpPacketHelper helper = getLauncher().getPacketHelper();
//		PockItem[] array = helper.getPockItems();
//		for (PockItem item : array) {
//			print(true, local, item);
//		}
//	}
	
	/**
	 * 打印结果
	 * @param control 控制信道
	 * @param item
	 */
	private void print(int family, SocketHost local, PockItem item) {
		ShowItem showItem = new ShowItem();

		// 1. 类型
		if (family == DesktopCheckPockChannelInvoker.CONTROL) {
			Color color = findXMLForeground("CHECK-POCK-CHANNEL/TYPE/CONTROL");
			String name = getXMLContent("CHECK-POCK-CHANNEL/TYPE/CONTROL");
			showItem.add(new ShowStringCell(0, name, color));
		} else if(family == DesktopCheckPockChannelInvoker.SUCK){
			Color color = findXMLForeground("CHECK-POCK-CHANNEL/TYPE/SUCK-DATA");
			String name = getXMLContent("CHECK-POCK-CHANNEL/TYPE/SUCK-DATA");
			showItem.add(new ShowStringCell(0, name, color));
		} else if(family == DesktopCheckPockChannelInvoker.DISPATCH) {
			Color color = findXMLForeground("CHECK-POCK-CHANNEL/TYPE/DISPATCH-DATA");
			String name = getXMLContent("CHECK-POCK-CHANNEL/TYPE/DISPATCH-DATA");
			showItem.add(new ShowStringCell(0, name, color));
		}
		
		//2. 本地地址
		showItem.add(new ShowStringCell(1, local.toString()));
		
		if (item != null) {
			// 3. NAT转义地址
			showItem.add(new ShowStringCell(2, item.getLocalNAT()));
			// 4.服务器
			showItem.add(new ShowStringCell(3, item.getRemote()));
			// 5. 节点主机
			if (item.getHub() != null) {
				showItem.add(new ShowStringCell(4, item.getHub()));
			} else {
				showItem.add(new ShowStringCell(4, ""));
			}
		} else {
			showItem.add(new ShowStringCell(2, ""));
			showItem.add(new ShowStringCell(3, ""));
			showItem.add(new ShowStringCell(4, ""));
		}
		addShowItem(showItem);
	}

	/**
	 * 显示数据信道，包括发送和接收
	 */
	private void checkDataPock() {
		SocketHost sucker = getLauncher().getSuckerBindHost(); // 真实地址
		SocketHost dispatcher = getLauncher().getDispatcherBindHost(); // 真实地址
		ReplyHelper helper = getLauncher().getReplyHelper();
		PockItem[] array = helper.getPockItems();
		if (array != null && array.length > 0) {
			// 发送信道
			print(DISPATCH, dispatcher, null);
			// 接收信道
			for (PockItem item : array) {
				print(SUCK, sucker, item);
			}
		}
	}

	/**
	 * 显示控制信道
	 */
	private void checkControlPock() {
		SocketHost local = getLauncher().getPacketBindHost(); // 本地真实地址
		FixpPacketHelper helper = getLauncher().getPacketHelper();
		PockItem[] array = helper.getPockItems();
		if (array != null && array.length > 0) {
			for (PockItem item : array) {
				print(CONTROL, local, item);
			}
		}
	}
	
}