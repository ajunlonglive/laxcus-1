/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.account.dict.*;
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;

/**
 * 判断账号存在调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class AccountAssertUserInvoker extends AccountInvoker {

	/**
	 * 判断账号存在调用器，设置异步命令
	 * @param cmd 判断账号存在
	 */
	public AccountAssertUserInvoker(AssertUser cmd) {
		super(cmd);
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
		AssertUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		// 判断账号存在
		boolean success = StaffOnAccountPool.getInstance().hasAccount(siger);
		// 生成报告
		AssertUserProduct product = new AssertUserProduct(siger, success);
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