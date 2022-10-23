/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 判断账号存在调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class BankAssertUserInvoker extends BankInvoker {

	/**
	 * 构造判断账号存在调用器，指定命令
	 * @param cmd 判断账号存在命令
	 */
	public BankAssertUserInvoker(AssertUser cmd) {
		super(cmd);
	}

	/**
	 * 向请求端反馈结果
	 * @param success
	 */
	private void reply(boolean success) {
		AssertUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		// 反馈结果
		AssertUserProduct product = new AssertUserProduct(siger, success);
		replyProduct(product);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertUser getCommand() {
		return (AssertUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 检测账号存在
		AssertUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		boolean success = FaultOnBankPool.getInstance().hasAccount(siger);
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
					AssertUserProduct e = getObject(AssertUserProduct.class, index);
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
		// 反馈结果
		reply(success);

		return useful(success);
	}

}