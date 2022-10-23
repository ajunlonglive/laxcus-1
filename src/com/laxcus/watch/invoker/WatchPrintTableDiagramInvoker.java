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
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示数据表状态的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class WatchPrintTableDiagramInvoker extends WatchPrintResourceDiagramInvoker {

	/**
	 * 构造显示数据表状态的异步调用器，指定命令
	 * @param cmd 显示数据表状态
	 */
	public WatchPrintTableDiagramInvoker(PrintTableDiagram cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintTableDiagram getCommand() {
		return (PrintTableDiagram) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PrintTableDiagram cmd = getCommand();
		// 在WATCH站点，不能显示自己
		if (cmd.isMe()) {
			faultX(FaultTip.PERMISSION_MISSING);
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
	 * 
	 * @param product
	 */
	private void print(List<Account> accounts) {
		printRuntime();
		printTitle();

		PrintTableDiagram cmd = getCommand();
		List<Siger> sigers = cmd.getUsers();
		int size = sigers.size();
		for (int index = 0; index < size; index++) {
			// 空格
			if (index > 0) printGap();

			Siger siger = sigers.get(index);
			String username = cmd.findPlainText(siger);
			Account account = find(siger, accounts);
			
			// 没有找到，显示错误
			if (account == null) {
				printNotFound(username); // 弹出错误
			} else {
				List<Table> array = account.getTables();
				if (array.isEmpty()) {
					printEmpty(username);
				} else {
					printTables(username, account.getTables());
				}
			}
		}
		// 输出全部记录
		flushTable();
	}

}