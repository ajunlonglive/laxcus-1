/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.permit.*;
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
public class WatchPrintGrantDiagramInvoker extends WatchInvoker {

	/**
	 * 构造显示注册用户状态的异步调用器，指定命令
	 * @param cmd 显示注册用户状态
	 */
	public WatchPrintGrantDiagramInvoker(PrintGrantDiagram cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintGrantDiagram getCommand() {
		return (PrintGrantDiagram) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PrintGrantDiagram cmd = getCommand();
		// 如果是显示自己，是错误
		if(cmd.isMe()) {
			faultX(FaultTip.EMPRY_LIST);
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
	 * 显示参数
	 * @param accounts
	 */
	private void print(List<Account> accounts) {
		PrintGrantDiagram cmd = getCommand();

		printTitle();

		List<Siger> sigers = cmd.getUsers();
		int size = sigers.size();
		for (int index = 0; index < size; index++) {
			if (index > 0) printGap();

			Siger siger = sigers.get(index);
			String username = cmd.findPlainText(siger);

			Account account = find(siger, accounts);
			// 没找到，显示空格
			if (account == null) {
				printNotFound(username);
			} else {
				print(username, account);
			}
		}
		
		// 输出全部记录
		flushTable();
	}
	
	private String[] getTitleCells() {
		return new String[] { "GRANT-DIAGRAM/USERNAME", "GRANT-DIAGRAM/COMMAND",
				"GRANT-DIAGRAM/REMARK", "GRANT-DIAGRAM/RANK" };
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
	 * 显示账号
	 * @param username 用户明文
	 * @param account 账号
	 */
	private void print(String username, Account account) {
		UserPermit userPermit = account.getUserPermit();
		SchemaPermit schamePermit = account.getSchemaPermit();
		TablePermit tablePermit = account.getTablePermit();

		int count = 0;
		if (userPermit.size() > 0) {
			count += print(username, userPermit);
		}
		if (schamePermit.size() > 0) {
			count += print(username, schamePermit);
		}
		if (tablePermit.size() > 0) {
			count += print(username, tablePermit);
		}

		// 打印一个提示
		if (count == 0) {
			printEmpty(username);
		}
	}
	
	/**
	 * 打印没有找到
	 * @param username 用户名
	 */
	private void printNotFound(String username) {
		String remark = getXMLContent("GRANT-DIAGRAM/USERNAME/NOTFOUND");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, username, java.awt.Color.RED));
		item.add(new ShowStringCell(1, " "));
		item.add(new ShowStringCell(2, remark, java.awt.Color.RED));
		item.add(new ShowStringCell(3, " "));
		addShowItem(item);
	}
	
	/**
	 * 打印空记录
	 * @param username 用户名
	 */
	private void printEmpty(String username) {
		String remark = getXMLContent("GRANT-DIAGRAM/USERNAME/EMPTY");
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, username, java.awt.Color.BLUE));
		item.add(new ShowStringCell(1, " "));
		item.add(new ShowStringCell(2, remark, java.awt.Color.BLUE));
		item.add(new ShowStringCell(3, " "));
		addShowItem(item);
	}
	
	/**
	 * 打印参数
	 * @param permit 用户权限
	 * @return 返回行记录数目
	 */
	private int print(String username, UserPermit permit) {
		String rank = getXMLContent("GRANT-DIAGRAM/RANK/USER");

		List<java.lang.Short> list = permit.list();
		for (short operator : list) {
			ShowItem item = new ShowItem();
			String symbol = ControlTag.translate(operator);

			item.add(new ShowStringCell(0, username));
			item.add(new ShowStringCell(1, symbol));
			item.add(new ShowStringCell(2, " "));
			item.add(new ShowStringCell(3, rank));
			addShowItem(item);
		}
		return list.size();
	}
	
	/**
	 * 打印数据库参数
	 * @param permit 数据库权限
	 * @return 返回行记录数目
	 */
	private int print(String username, SchemaPermit permit) {
		String rank = getXMLContent("GRANT-DIAGRAM/RANK/DATABASE");
		
		int count = 0;
		Set<Fame> keys = permit.keys();
		for(Fame fame : keys) {
			Control control = permit.find(fame);
			for(short operator : control.list()) {
				ShowItem item = new ShowItem();
				String symbol = ControlTag.translate(operator);

				item.add(new ShowStringCell(0, username));
				item.add(new ShowStringCell(1, symbol));
				item.add(new ShowStringCell(2, fame));
				item.add(new ShowStringCell(3, rank));
				addShowItem(item);
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 打印数据表
	 * @param permit 表权限
	 * @return 返回行记录数目
	 */
	private int print(String username, TablePermit permit) {
		String rank = getXMLContent("GRANT-DIAGRAM/RANK/TABLE");
		
		int count = 0;
		Set<Space> keys = permit.keys();
		for(Space space : keys) {
			Control control = permit.find(space);
			for(short operator : control.list()) {
				ShowItem item = new ShowItem();
				String symbol = ControlTag.translate(operator);

				item.add(new ShowStringCell(0, username));
				item.add(new ShowStringCell(1, symbol));
				item.add(new ShowStringCell(2, space));
				item.add(new ShowStringCell(3, rank));
				addShowItem(item);
				count++;
			}
		}
		return count;
	}

}