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
 * 删除数据库调用器。<br>
 * 数据库只能由账号所有者才能删除。
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class AccountDropSchemaInvoker extends AccountInvoker {

	/**
	 * 删除数据库调用器，设置异步命令
	 * @param cmd 删除数据库
	 */
	public AccountDropSchemaInvoker(DropSchema cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSchema getCommand() {
		return (DropSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropSchema cmd = getCommand();
		Fame fame = cmd.getFame(); // 数据库名
		Siger siger = cmd.getIssuer();

		// 删除数据库
		boolean success = StaffOnAccountPool.getInstance().dropSchema(siger, fame);

		// 设置状态结果
		DropSchemaProduct product = new DropSchemaProduct(fame, success);

		// 成功
		if (success) {
			replyProduct(product);
		} else {
			failed();
		}

		Logger.debug(this, "launch", success, "drop %s", fame);

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