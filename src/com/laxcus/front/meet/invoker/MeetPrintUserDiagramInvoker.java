/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 显示注册用户状态的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class MeetPrintUserDiagramInvoker extends MeetInvoker {

	/**
	 * 构造显示注册用户状态的异步调用器，指定命令
	 * @param cmd 显示注册用户状态
	 */
	public MeetPrintUserDiagramInvoker(PrintUserDiagram cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintUserDiagram getCommand() {
		return (PrintUserDiagram) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PrintUserDiagram cmd = getCommand();
		//		// 不是显示自己，显示权限不足的提示。
		//		if (!cmd.isMe()) {
		//			faultX(FaultTip.PERMISSION_MISSING);
		//			return false;
		//		} else if (isAdministrator()) {
		//			faultX(FaultTip.SYSTEM_DENIED);
		//			return false;
		//		}

		if (isAdministrator()) {
			faultX(FaultTip.SYSTEM_DENIED);
			return false;
		}
		// 不是显示自己，显示权限不足的提示。
		if (!cmd.isMe()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}

		Account account = getStaffPool().getAccount();
		print(account.getUser());
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
	 * 生成标题单元
	 */
	private void printTitle() {
		// 生成表格标题
		String[] cells = new String[] { "USER-DIAGRAM/ATTRIBUTE",
				"USER-DIAGRAM/VALUE" };
		createShowTitle(cells);
	}
	
	/**
	 * 显示一行
	 * @param xmlPath
	 * @param value
	 */
	private void printItem(String xmlPath, String value) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlPath);
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, value));
		addShowItem(item);
	}
	
	/**
	 * 显示一行
	 * @param xmlPath
	 * @param value
	 * @param color 前景颜色
	 */
	private void printItem(String xmlPath, String value, java.awt.Color color) {
		ShowItem item = new ShowItem();
		String name = findXMLTitle(xmlPath);
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, value, color));
		addShowItem(item);
	}
	
//	/**
//	 * 显示一行
//	 * @param xmlPath
//	 * @param success
//	 */
//	private void printItem(String xmlPath, boolean success) {
//		ShowItem item = new ShowItem();
//		String name = findXMLTitle(xmlPath);
//		item.add(new ShowStringCell(0, name));
//		item.add(createConfirmTableCell(1, success));
//		addShowItem(item);
//	}
	
	/**
	 * 显示一行
	 * @param xmlPath
	 * @param value
	 */
	private void printItem(String xmlPath, Object value) {
		printItem(xmlPath, value.toString());
	}
	
	/**
	 * 显示一行
	 * @param xmlPath
	 * @param value
	 */
	private void printItem(String xmlPath, long value) {
		String text = String.format("%d", value);
		printItem(xmlPath, text);
	}
	
	/**
	 * 解析空间容量
	 * @param size
	 * @return
	 */
	private String splitCapacity(long size) {
		return ConfigParser.splitCapacity(size);
	}

//	/**
//	 * 解析时间参数
//	 * @param time
//	 * @return
//	 */
//	private String splitCreateTime(long time) {
//		Date date = com.laxcus.util.datetime.SimpleTimestamp.format(time);
//		DateFormat dt =  DateFormat.getDateTimeInstance(); // 系统默认的日期/时间格式
//		return dt.format(date);
//	}
	
	/**
	 * 打印记录
	 * @param user
	 */
	private void print(User user) {
		printTitle();
		printItem("USER-DIAGRAM/ITEM/SIGER", user.getUsername());
		printItem("USER-DIAGRAM/ITEM/CREATE-TIME", splitLaxcusTime(user.getCreateTime()));
		// 到期时间
		long time = user.getExpireTime();
		if (time < 1) {
			String value = getXMLContent("USER-DIAGRAM/ITEM/EXPIRE-TIME/UNLIMIT");
			printItem("USER-DIAGRAM/ITEM/EXPIRE-TIME", value);
		} else {
			printItem("USER-DIAGRAM/ITEM/EXPIRE-TIME", splitLaxcusTime(time));
		}
		// 开放状态或者否
		if (user.isOpening()) {
			String value =  getXMLContent("USER-DIAGRAM/ITEM/USING/OPENING");
			printItem("USER-DIAGRAM/ITEM/USING", value);
		} else {
			String value = getXMLContent("USER-DIAGRAM/ITEM/USING/CLOSED");
			printItem("USER-DIAGRAM/ITEM/USING", value, java.awt.Color.RED);
		}

		// 占用空间
		long size = user.getMaxSize();
		if (size == 0) {
			String value = getXMLContent("USER-DIAGRAM/ITEM/CAPACITY/UNLIMIT");
			printItem("USER-DIAGRAM/ITEM/CAPACITY", value);
		} else {
			printItem("USER-DIAGRAM/ITEM/CAPACITY", splitCapacity(size));
		}

		// 操作权极
		String priority = CommandPriority.translate((byte) user.getPriority());
		printItem("USER-DIAGRAM/ITEM/PRIORITY", priority);
		// 在线用户/并行任务数目
		printItem("USER-DIAGRAM/ITEM/MEMBERS", user.getMembers());
		printItem("USER-DIAGRAM/ITEM/JOBS", user.getJobs());
		// 集群
		printItem("USER-DIAGRAM/ITEM/GROUPS", user.getGroups());
		// CALL（网关）/WORK/BUILD节点
		printItem("USER-DIAGRAM/ITEM/BASES", user.getBases());
		printItem("USER-DIAGRAM/ITEM/SUBBASES", user.getSubBases());
		printItem("USER-DIAGRAM/ITEM/GATEWAYS", user.getGateways());
		printItem("USER-DIAGRAM/ITEM/WORKERS", user.getWorkers());
		printItem("USER-DIAGRAM/ITEM/BUILDERS", user.getBuilders());
		// 最大应用软件规模
		printItem("USER-DIAGRAM/ITEM/APPLICATIONS", user.getTasks());
		// 数据块尺寸
		printItem("USER-DIAGRAM/ITEM/CHUNK-SIZE", splitCapacity(user.getChunkSize()));
		// 表数目
		int tables = user.getTables();
		if (tables == 0) {
			String value = getXMLContent("USER-DIAGRAM/ITEM/TABLE-SIZE/UNLIMIT");
			printItem("USER-DIAGRAM/ITEM/TABLE-SIZE", value);
		} else {
			printItem("USER-DIAGRAM/ITEM/TABLE-SIZE", tables);
		}
		// 索引数目
		int indexes = user.getIndexes();
		if (indexes == 0) {
			String value = getXMLContent("USER-DIAGRAM/ITEM/INDEX-SIZE/UNLIMIT");
			printItem("USER-DIAGRAM/ITEM/INDEX-SIZE", value);
		} else {
			printItem("USER-DIAGRAM/ITEM/INDEX-SIZE", indexes);
		}
		// 云存储空间
		long cloudSize = user.getCloudSize();
		if (cloudSize == 0) {
			String value = getXMLContent("USER-DIAGRAM/ITEM/CLOUD-SIZE/LIMIT");
			printItem("USER-DIAGRAM/ITEM/CLOUD-SIZE", value);
		} else {
			printItem("USER-DIAGRAM/ITEM/CLOUD-SIZE", ConfigParser.splitCapacity( cloudSize));
		}
		
		// 输出全部记录
		flushTable();
	}

}