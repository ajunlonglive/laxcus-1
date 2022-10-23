/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.bank.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 查表命令调用器。<br>
 * 命令从TOP站点传来，去全部ACCOUNT站点查询一个数据表。
 * 
 * @author scott.liang
 * @version 1.0 7/9/2018
 * @since laxcus 1.0
 */
public class BankTakeTableInvoker extends BankSeekAccountSiteInvoker {

	/**
	 * 构造查表命令调用器，指定命令
	 * @param cmd 查表命令
	 */
	public BankTakeTableInvoker(TakeTable cmd) {
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
		
		List<Node> slaves = AccountOnBankPool.getInstance().detail();
		// 向全部ACCOUNT站点群发查表命令
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);
		if (!success) {
			failed();
		}
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		Table table = null;
		
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					table = getObject(Table.class, index);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断有效
		boolean success = (table != null);
		if (success) {
			super.replyObject(table);
		} else {
			failed();
		}

		return useful(success);
	}

}
