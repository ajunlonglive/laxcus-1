/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.access.table.*;

/**
 * 获取数据表调用器。<br>
 * GATE发出，ACCOUNT接收。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class AccountTakeTableInvoker extends AccountInvoker {

	/**
	 * 构造获取数据表，指定命令
	 * @param cmd 获取数据表命令
	 */
	public AccountTakeTableInvoker(TakeTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeTable getCommand() {
		return (TakeTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeTable cmd = getCommand();
		Space space = cmd.getSpace();

		// 查找数据表
		Table table = StaffOnAccountPool.getInstance().readTable(space);
		boolean success = (table != null);
		if (success) {
			success = replyObject(table);
		}
		if (!success) {
			failed();
		}
		
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}