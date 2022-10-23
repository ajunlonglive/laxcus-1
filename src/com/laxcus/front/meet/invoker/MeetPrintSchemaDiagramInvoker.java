/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.schema.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示数据库状态的异步调用器
 * 
 * @author scott.liang
 * @version 1.0 02/12/2018
 * @since laxcus 1.0
 */
public class MeetPrintSchemaDiagramInvoker extends MeetPrintResourceDiagramInvoker {

	/**
	 * 构造显示数据库状态的异步调用器，指定命令
	 * @param cmd 显示数据库状态
	 */
	public MeetPrintSchemaDiagramInvoker(PrintSchemaDiagram cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintSchemaDiagram getCommand() {
		return (PrintSchemaDiagram) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PrintSchemaDiagram cmd = getCommand();
		// FRONT站点只能显示自己的数据库
		if (!cmd.isMe()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		} else if (isAdministrator()) {
			faultX(FaultTip.SYSTEM_DENIED);
			return false;
		}
		
		ShowSchema sub = new ShowSchema();
		sub.addAll(cmd.list());
		
		// 投递到AID站点
		return fireToHub(sub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SchemaProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SchemaProduct.class, index);
			}
		} catch (VisitException e) {
			fault(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			printSchemas(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

}
