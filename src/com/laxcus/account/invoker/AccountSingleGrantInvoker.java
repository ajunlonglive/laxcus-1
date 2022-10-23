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
import com.laxcus.command.access.permit.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 单账号授权调用器。<br>
 * 来自GATE站点，向某个账号授权。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class AccountSingleGrantInvoker extends AccountInvoker {

	/**
	 * 构造单账号授权调用器，指定命令
	 * @param cmd 单账号授权
	 */
	public AccountSingleGrantInvoker(SingleGrant cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SingleGrant getCommand() {
		return (SingleGrant) super.getCommand();
	}

	/**
	 * 向请求端反馈结果
	 * @param success 成功或者否
	 */
	protected void reply(boolean success) {
		SingleGrant cmd = getCommand();
		Seat seat = new Seat(cmd.getSiger(), getLocal());
		SignleCertificateProduct product = new SignleCertificateProduct(seat, success);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SingleGrant cmd = getCommand();
		Siger siger = cmd.getSiger();
		Permit permit =	cmd.getPermit();

		// 读出账号，向账号授权
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		boolean success = (sphere != null);
		if (success) {
			success = sphere.getAccount().grant(permit);
			if (success) {
				success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
			}
		}

		//		// 向账号授权
		//		boolean success = AccountPool.getInstance().grant(siger, permit);
		//		// 修改成功，通过BANK向HASH/GATE站点广播一个更新（在此HASH是不需要的冗余）
		//		if(success) {
		//			multicast(siger);
		//		}

		// 结果反馈给GATE
		reply(success);

		Logger.debug(this, "launch", success, "grant %s", siger);

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