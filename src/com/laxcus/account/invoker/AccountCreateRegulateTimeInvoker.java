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
import com.laxcus.command.rebuild.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 建立数据优化时间命令调用器
 * 
 * @author scott.liang
 * @version 1.0 7/23/2013
 * @since laxcus 1.0
 */
public class AccountCreateRegulateTimeInvoker extends AccountInvoker {

	/**
	 * 构造建立数据优化时间命令调用器，指定命令
	 * @param cmd 建立数据优化时间命令
	 */
	public AccountCreateRegulateTimeInvoker(CreateRegulateTime cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateRegulateTime getCommand() {
		return (CreateRegulateTime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateRegulateTime cmd = getCommand();
		Siger siger = getIssuer();
		SwitchTime switchTime = cmd.getSwitchTime();
		
		// 读资源
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		boolean success = (sphere != null);
		if (success) {
			// 判断没有超过最大数目
			int regulates = sphere.getAccount().getUser().getRegulates();
			success = (sphere.getAccount().countSwitchTimes() < regulates);
			// 成功
			if (success) {
				success = sphere.getAccount().createSwitchTime(switchTime);
				if (success) {
					StaffOnAccountPool.getInstance().addSwitchTime(sphere, switchTime);
					success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
				}
			}
		}

		// 返回执行结果
		CreateRegulateTimeProduct product = new CreateRegulateTimeProduct(switchTime, success);
		replyProduct(product);

		Logger.debug(this, "launch", success, "switch time is %s", switchTime);

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
