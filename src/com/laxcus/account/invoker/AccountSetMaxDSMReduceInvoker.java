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
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * DSM表最大压缩倍数调用器
 * 
 * @author scott.liang
 * @version 1.0 6/2/2019
 * @since laxcus 1.0
 */
public class AccountSetMaxDSMReduceInvoker extends AccountInvoker {

	/**
	 * 构造DSM表最大压缩倍数调用器，指定命令
	 * @param cmd DSM表最大压缩倍数
	 */
	public AccountSetMaxDSMReduceInvoker(SetMaxDSMReduce cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMaxDSMReduce getCommand() {
		return (SetMaxDSMReduce) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetMaxDSMReduce cmd = getCommand();
		Siger siger = cmd.getSiger(); // 账号签名
		Space space = cmd.getSpace();
		
		// 读出账号，修改参数
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		boolean success = (sphere != null);
		if (success) {
			Table table = sphere.getAccount().findTable(space);
			success = (table != null);
			if (success) {
				table.setMultiple(cmd.getMultiple());
				success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
			}
		}

		// 反馈结果
		SetMaxDSMReduceProduct product = new SetMaxDSMReduceProduct(success);
		replyProduct(product);
		
		Logger.debug(this, "launch", success, "update %s %s %d", siger, space, cmd.getMultiple());

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
