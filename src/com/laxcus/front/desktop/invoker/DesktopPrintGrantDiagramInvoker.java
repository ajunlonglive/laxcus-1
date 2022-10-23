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
import com.laxcus.command.access.permit.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 显示注册用户状态的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 02/21/2021
 * @since laxcus 1.0
 */
public class DesktopPrintGrantDiagramInvoker extends DesktopPrintResourceDiagramInvoker {

	/**
	 * 构造显示注册用户状态的异步调用器，指定命令
	 * @param cmd 显示注册用户状态
	 */
	public DesktopPrintGrantDiagramInvoker(PrintGrantDiagram cmd) {
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
		// 不是显示自己，显示权限不足的提示。
		if (!cmd.isMe()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		} else if (isAdministrator()) {
			faultX(FaultTip.SYSTEM_DENIED);
			return false;
		}
		
		Account account = getStaffPool().getAccount();

		UserPermit userPermit = account.getUserPermit();
		SchemaPermit schamePermit = account.getSchemaPermit();
		TablePermit tablePermit = account.getTablePermit();

		int count = 0;
		
		printTitle();
		if (userPermit.size() > 0) {
			count = print(userPermit);
		}
		if (schamePermit.size() > 0) {
			if (count > 0) printGap();
			count = print(schamePermit);
		}
		if (tablePermit.size() > 0) {
			if (count > 0) printGap();
			print(tablePermit);
		}
		
		// 输出全部记录
		flushTable();

		// 退出
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
	 * 输出标题单元
	 * @return
	 */
	private String[] getTitleCells() {
		String[] cells = new String[] { "GRANT-DIAGRAM/COMMAND",
				"GRANT-DIAGRAM/REMARK", "GRANT-DIAGRAM/RANK" };
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
	 * 打印参数
	 * @param permit
	 */
	private int print(UserPermit permit) {
		String rank = getXMLContent("GRANT-DIAGRAM/RANK/USER");

		List<java.lang.Short> list = permit.list();
		for (short operator : list) {
			ShowItem item = new ShowItem();
			String name = ControlTag.translate(operator);

			item.add(new ShowStringCell(0, name));
			if (isDesktop()) {
				item.add(new ShowStringCell(1, "|  |"));
			} else {
				item.add(new ShowStringCell(1, " "));
			}
			item.add(new ShowStringCell(2, rank));
			addShowItem(item);
		}
		return list.size();
	}
	
	/**
	 * 打印数据库参数
	 * @param permit
	 */
	private int print(SchemaPermit permit) {
		String rank = getXMLContent("GRANT-DIAGRAM/RANK/DATABASE");
		
		int count = 0;
		Set<Fame> keys = permit.keys();
		for(Fame fame : keys) {
			Control control = permit.find(fame);
			for(short operator : control.list()) {
				ShowItem item = new ShowItem();
				String name = ControlTag.translate(operator);

				item.add(new ShowStringCell(0, name));
				item.add(new ShowStringCell(1, fame));
				item.add(new ShowStringCell(2, rank));
				addShowItem(item);
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 打印数据表
	 * @param permit
	 */
	private int print(TablePermit permit) {
		String rank = getXMLContent("GRANT-DIAGRAM/RANK/TABLE");
		
		int count = 0;
		Set<Space> keys = permit.keys();
		for(Space space : keys) {
			Control control = permit.find(space);
			for(short operator : control.list()) {
				ShowItem item = new ShowItem();
				String name = ControlTag.translate(operator);

				item.add(new ShowStringCell(0, name));
				item.add(new ShowStringCell(1, space));
				item.add(new ShowStringCell(2, rank));
				addShowItem(item);
				count++;
			}
		}
		return count;
	}

}