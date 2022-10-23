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
 * 判断数据表存在调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class BankAssertTableInvoker extends BankInvoker {

	/**
	 * 构造判断数据表存在调用器，指定命令
	 * @param cmd 判断数据表存在命令
	 */
	public BankAssertTableInvoker(AssertTable cmd) {
		super(cmd);
	}
	
	/**
	 * 向请求端反馈结果
	 * @param success
	 */
	private void reply(boolean success) {
		AssertTable cmd = getCommand();
		Space space = cmd.getSpace();
		// 反馈结果
		AssertTableProduct product = new AssertTableProduct(space, success);
		replyProduct(product);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertTable getCommand() {
		return (AssertTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 检测故障数据库存在
		AssertTable cmd = getCommand();
		Space space = cmd.getSpace();
		boolean success = FaultOnBankPool.getInstance().hasTable(space);
		if (success) {
			reply(true);
			return false;
		}
		
		// 投递到全部ACCOUNT站点
		List<Node> slaves = AccountOnBankPool.getInstance().detail();
		success = launchTo(slaves);
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
		int count = 0;
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssertTableProduct e = getObject(AssertTableProduct.class, index);
					if (e.isSuccessful()) {
						count++;
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断找到
		boolean success = (count > 0);
		reply(success);

		return useful(success);
	}

}
