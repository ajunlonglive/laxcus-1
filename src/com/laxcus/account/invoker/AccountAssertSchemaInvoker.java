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

/**
 * 判断数据库存在调用器。<br>
 * 判断数据库存在，普通用户和管理员都可以查询。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class AccountAssertSchemaInvoker extends AccountInvoker {

	/**
	 * 判断数据库存在调用器，设置异步命令
	 * @param cmd 判断数据库存在
	 */
	public AccountAssertSchemaInvoker(AssertSchema cmd) {
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AssertSchema cmd = getCommand();
		Fame fame = cmd.getFame();
		// 判断数据库存在
		boolean success = StaffOnAccountPool.getInstance().hasSchema(fame);
		// 生成报告
		AssertSchemaProduct product = new AssertSchemaProduct(fame, success);
		// 反馈结果
		success = replyProduct(product);
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