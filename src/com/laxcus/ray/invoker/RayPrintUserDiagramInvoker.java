/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示注册用户状态的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class RayPrintUserDiagramInvoker extends RayInvoker {

	/**
	 * 构造显示注册用户状态的异步调用器，指定命令
	 * @param cmd 显示注册用户状态
	 */
	public RayPrintUserDiagramInvoker(PrintUserDiagram cmd) {
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
		// 空集合
		if(cmd.isMe()) {
			faultX(FaultTip.COMMAND_REFUSED);
			return false;
		}
		// 必须是BANK站点，否则不处理
		if (!isBankHub()) {
			faultX(FaultTip.BANK_RETRY);
			return false;
		}
		// 转成多个获取账号命令
		ArrayList<Command> array = new ArrayList<Command>();
		for(Siger siger : cmd.getUsers()) {
			TakeAccount sub = new TakeAccount(siger);
			array.add(sub);
		}
		// 容错模式发送
		return fireMultiToHub(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ArrayList<Account> array = new ArrayList<Account>();
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					TakeAccountProduct product = getObject(TakeAccountProduct.class, index);
					if (product != null && product.getAccount() != null) {
						array.add(product.getAccount());
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断成功
		boolean success = (array.size() > 0);
		if (success) {
			printRuntime();
			print(array);
		} else {
			printFault();
		}

		return useful(success);
	}
	
	/**
	 * 找到匹配的账号
	 * @param siger 账号签名
	 * @param array 账号数组
	 * @return 返回账号，或者空指针
	 */
	private Account find(Siger siger, List<Account> array) {
		for (Account e : array) {
			if (Laxkit.compareTo(siger, e.getUsername()) == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 打印结果
	 * @param accounts
	 */
	private void print(List<Account> accounts) {
		PrintUserDiagram cmd = getCommand();

		printTitle();

		java.util.List<Siger> sigers = cmd.getUsers();
		int size = sigers.size();
		for (int index = 0; index < size; index++) {
			if (index > 0) printGap();

			Siger siger = sigers.get(index);
			String username = cmd.findPlainText(siger);
			Account account = find(siger, accounts);
			
			// 打印结果
			if (account == null) {
				printNotFound(username);
			} else {
				User user = account.getUser();
				print(user, username);
			}
		}
		
		// 输出全部记录
		flushTable();
	}
	
	/**
	 * 单元
	 * @return
	 */
	private String[] getTitleCells() {
		String[] cells = new String[] { "USER-DIAGRAM/ATTRIBUTE",
				"USER-DIAGRAM/VALUE" };
		return cells;
	}
	
	/**
	 * 标题统计
	 * @return
	 */
	private int getTitleColumnsCount() {
		return getTitleCells().length;
	}
	
	/**
	 * 生成标题单元
	 */
	private void printTitle() {
		// 生成表格标题
		String[] cells = getTitleCells();
		createShowTitle(cells);
	}
	
	/**
	 * 打印没有找到
	 * @param username
	 */
	private void printNotFound(String username) {
		ShowItem item = new ShowItem();

		// 第一段
		String name = findXMLTitle("USER-DIAGRAM/USER");
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, username, java.awt.Color.RED));
		addShowItem(item);
		
		// 第二段
		item = new ShowItem();
		name = findXMLTitle("USER-DIAGRAM/USER/STATUS");
		String notfound = getXMLContent("USER-DIAGRAM/USER/STATUS/NOTFOUND");
		item.add(new ShowStringCell(0, name));
		item.add(new ShowStringCell(1, notfound, java.awt.Color.RED));
		addShowItem(item);
	}
	
	/**
	 * 打印空行
	 */
	private void printGap() {
		int count = getTitleColumnsCount();
		
		ShowItem item = new ShowItem();
		for (int i = 0; i < count; i++) {
			ShowStringCell e = new ShowStringCell(i, "  ");
			item.add(e);
		}
		addShowItem(item);
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
//	 * @param value
//	 */
//	private void printItem(String xmlPath, boolean success) {
//		ShowItem item = new ShowItem();
//		String name = findXMLTitle(xmlPath);
//		item.add(new ShowStringCell(0, name));
//		item.add(createConfirmTableCell(1, success));
//		addShowItem(item);
//	}
	
//	/**
//	 * 显示一行
//	 * @param xmlPath
//	 * @param value
//	 */
//	private void printItem(String xmlPath, Object value) {
//		printItem(xmlPath, value.toString());
//	}
	
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
	
	/**
	 * 打印记录
	 * @param user
	 * @param username 用户明文
	 */
	private void print(User user, String username) {
		printItem("USER-DIAGRAM/ITEM/SIGER", username);
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
		// CALL(网关)/WORK/BUILD
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
			printItem("USER-DIAGRAM/ITEM/CLOUD-SIZE",
					ConfigParser.splitCapacity(cloudSize));
		}

	}

}
