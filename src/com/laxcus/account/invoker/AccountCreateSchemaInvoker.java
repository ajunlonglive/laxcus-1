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
import com.laxcus.command.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 建立数据库调用器。<br>
 * 数据库只能由账号所有者才能建立。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class AccountCreateSchemaInvoker extends AccountInvoker {

	/**
	 * 建立数据库调用器，设置异步命令
	 * @param cmd 建立数据库
	 */
	public AccountCreateSchemaInvoker(CreateSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateSchema getCommand() {
		return (CreateSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateSchema cmd = getCommand();
		Schema schema = cmd.getSchema();
		Siger siger = cmd.getIssuer();

		// 建立数据库
		boolean success = StaffOnAccountPool.getInstance().createSchema(siger, schema);

		// 设置状态结果
		CreateSchemaProduct product = new CreateSchemaProduct(schema.getFame(), success);

		// 成功
		if (success) {
			replyProduct(product);
		} else {
			failed();
		}

		Logger.debug(this, "launch", success, "create %s", schema);

		// 退出
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