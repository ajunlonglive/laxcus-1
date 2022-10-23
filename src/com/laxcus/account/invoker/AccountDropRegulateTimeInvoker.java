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
 * 撤销数据优化时间命令调用器
 * 
 * @author scott.liang
 * @version 1.0 7/23/2013
 * @since laxcus 1.0
 */
public class AccountDropRegulateTimeInvoker extends AccountInvoker {

	/**
	 * 构造撤销数据优化时间命令调用器，指定命令
	 * @param cmd 撤销数据优化时间命令
	 */
	public AccountDropRegulateTimeInvoker(DropRegulateTime cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropRegulateTime getCommand() {
		return (DropRegulateTime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropRegulateTime cmd = getCommand();
		Siger siger = getIssuer();
		Space space = cmd.getSpace();

		// 删除数据优化时间
		AccountSphere sphere = StaffOnAccountPool.getInstance().readAccountSphere(siger);
		boolean success = (sphere != null);
		if (success) {
			SwitchTime time = sphere.getAccount().findSwitchTime(space);
			success = (time != null);
			if (success) {
				success = sphere.getAccount().dropSwitchTime(space);
				if (success) {
					success = StaffOnAccountPool.getInstance().updateAccountSphere(sphere);
					if(success) {
						StaffOnAccountPool.getInstance().removeSwitchTime(sphere, time);
					}
				}
			}
		}

		// 返回执行结果
		DropRegulateTimeProduct product = new DropRegulateTimeProduct(space, success);
		replyProduct(product);

		Logger.debug(this, "launch", success, "table is %s", space);

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
