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
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 开放/解禁用户账号调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 1/5/2020
 * @since laxcus 1.0
 */
public class AccountOpenUserInvoker extends AccountInvoker {

	/**
	 * 构造开放/解禁用户账号调用器，指定命令
	 * @param cmd 开放/解禁用户账号
	 */
	public AccountOpenUserInvoker(OpenUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public OpenUser getCommand() {
		return (OpenUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		OpenUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		
		// 读资源
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		boolean success = (sphere != null);
		if (success) {
			sphere.getAccount().getUser().setClosed(false);
			success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
		}

		// 结果反馈给BANK
		OpenUserProduct product = new OpenUserProduct(siger, success);
		replyProduct(product);
		
		Logger.debug(this, "launch", success, "open account: %s", siger);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}