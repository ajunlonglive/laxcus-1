/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 修改账号调用器。<br>
 * 修改注册用户的账号密码。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class AccountAlterUserInvoker extends AccountInvoker {

	/**
	 * 构造修改账号调用器，指定命令
	 * @param cmd 修改账号
	 */
	public AccountAlterUserInvoker(AlterUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AlterUser getCommand() {
		return (AlterUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AlterUser cmd = getCommand();
		User user = cmd.getUser();
		Siger siger = user.getUsername();
		
		// 读资源
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		boolean success = (sphere != null);
		if (success) {
			sphere.getAccount().getUser().setPassword(user.getPassword());
			success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
		}

		// 结果反馈给BANK
		AlterUserProduct product = new AlterUserProduct(siger, success);
		replyProduct(product);
		
		Logger.debug(this, "launch", success, "%s alter  %s", getIssuer(), user);

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