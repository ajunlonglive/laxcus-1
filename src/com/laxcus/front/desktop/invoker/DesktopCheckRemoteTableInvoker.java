/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.site.*;

/**
 * 检测本地注册表异步调用器。
 * 
 * @author scott.liang
 * @version 1.0 05/29/2021
 * @since laxcus 1.0
 */
public class DesktopCheckRemoteTableInvoker extends DesktopInvoker {

	/**
	 * 构造检测本地注册表异步调用器，指定命令
	 * @param cmd 显示数据表命令
	 */
	public DesktopCheckRemoteTableInvoker(CheckRemoteTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckRemoteTable getCommand() {
		return (CheckRemoteTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	public boolean launch() {
		// 系统管理员不能操作这个命令
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful();
		}
		
		TreeSet<Space> array = new TreeSet<Space>();

		CheckRemoteTable cmd = getCommand();
		if (cmd.isAll()) {
			Account account = getStaffPool().getAccount();
			array.addAll(account.getSpaces());
			array.addAll(account.getPassiveTables());
			array.addAll(account.getActiveTables());
		} else {
			array.addAll(cmd.list());
		}
		
		// 显示标题
		printTitle();
		
		String own = getXMLContent("CHECK-REMOTE-TABLE/ATTRIBUTE/OWN");
		String passive = getXMLContent("CHECK-REMOTE-TABLE/ATTRIBUTE/PASSIVE");
		
		// 显示
		for(Space space: array) {
			// 判断是被授权表
			boolean yes = getStaffPool().isPassiveTable(space);
			
			NodeSet set = getStaffPool().findTableSites(space);
			if (set == null || set.isEmpty()) {
				ShowItem item = new ShowItem();
				// 图标
				item.add(createConfirmTableCell(0, false));
				// 表名
				item.add(new ShowStringCell(1, space));
				// 属性（自有/被授权）
				item.add(new ShowStringCell(2, (yes ? passive : own)));
				// 没有地址
				item.add(new ShowStringCell(3, " "));
				addShowItem(item);
			} else {
				for (Node hub : set.show()) {
					ShowItem item = new ShowItem();
					// 图标
					item.add(createConfirmTableCell(0, true));
					item.add(new ShowStringCell(1, space));
					item.add(new ShowStringCell(2, (yes ? passive : own)));
					item.add(new ShowStringCell(3, hub.toString()));
					addShowItem(item);
				}
			}
		}
		
		// 输出全部记录
		flushTable();
		
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 输出标题单元
	 * @return
	 */
	private String[] getTitleCells() {
		// 生成表格标题
		String[] cells = new String[] { "CHECK-REMOTE-TABLE/STATUS",
				"CHECK-REMOTE-TABLE/TABLE", "CHECK-REMOTE-TABLE/ATTRIBUTE", "CHECK-REMOTE-TABLE/SITE" };
		return cells;
	}

	/**
	 * 生成标题单元
	 */
	private void printTitle() {
		String[] cells = getTitleCells();
		createShowTitle(cells);
	}

	
}