/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示数据表状态的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class MeetPrintTableDiagramInvoker extends MeetPrintResourceDiagramInvoker {

	/**
	 * 构造显示数据表状态的异步调用器，指定命令
	 * @param cmd 显示数据表状态
	 */
	public MeetPrintTableDiagramInvoker(PrintTableDiagram cmd) {
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

		// FRONT站点只能显示自己的数据表
		if (!cmd.isMe()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		} else if (isAdministrator()) {
			faultX(FaultTip.SYSTEM_DENIED);
			return false;
		}

		ShowTable sub = new ShowTable();
		sub.addAll(cmd.list());

		// 投递到AID站点
		return fireToHub(sub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TableProduct.class, index);
			}
		} catch (VisitException e) {
			super.fault(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			printTables(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

}
