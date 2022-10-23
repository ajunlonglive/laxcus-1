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
import com.laxcus.command.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 判断数据库存在调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class BankAssertSchemaInvoker extends BankInvoker {

	/**
	 * 构造判断数据库存在调用器，指定命令
	 * @param cmd 判断数据库存在命令
	 */
	public BankAssertSchemaInvoker(AssertSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertSchema getCommand() {
		return (AssertSchema) super.getCommand();
	}

	/**
	 * 向请求端反馈结果
	 * @param success
	 */
	private void reply(boolean success) {
		AssertSchema cmd = getCommand();
		Fame fame = cmd.getFame();
		// 反馈结果
		AssertSchemaProduct product = new AssertSchemaProduct(fame, success);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 检测故障数据库存在
		AssertSchema cmd = getCommand();
		boolean success = FaultOnBankPool.getInstance().hasSchema(cmd.getFame());
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
					AssertSchemaProduct e = getObject(AssertSchemaProduct.class, index);
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

		//		Logger.debug(this, "ending", success, "%s", fame);
		//		// 反馈结果
		//		AssertSchemaProduct product = new AssertSchemaProduct(fame, success);
		//		replyProduct(product);


		return useful(success);
	}

}
