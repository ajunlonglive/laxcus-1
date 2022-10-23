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
 * 设置账号最多优化表数目调用器
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class AccountSetSingleMaxRegulatesInvoker extends AccountInvoker {

	/**
	 * 构造设置账号最多优化表数目调用器，指定命令
	 * @param cmd 设置账号最多优化表数目
	 */
	public AccountSetSingleMaxRegulatesInvoker(SetSingleMaxRegulates cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetSingleMaxRegulates getCommand() {
		return (SetSingleMaxRegulates) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetSingleMaxRegulates cmd = getCommand();
		Siger siger = cmd.getSiger(); // 账号签名

		// 读出账号，修改参数
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		boolean success = (sphere != null);
		if (success) {
			sphere.getAccount().getUser().setRegulates(cmd.getRegulates());
			success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
		}
		
		// 反馈结果
		RefreshItem item = new RefreshItem(siger, success);
		replyObject(item);
		
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
