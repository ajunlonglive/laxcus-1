/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 推送新注册站点命令调用器
 * 
 * @author scott.liang
 * @version 1.0 6/28/2018
 * @since laxcus 1.0
 */
public class BankPushSiteInvoker extends BankInvoker {

	/**
	 * 构造推送新注册站点命令调用器，指定命令
	 * @param cmd 推送新注册站点命令
	 */
	public BankPushSiteInvoker(PushSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushSite getCommand() {
		return (PushSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PushSite cmd = getCommand();
		// 判断是监视站点
		boolean success = isMonitor();
		// 保存这个站点
		if (success) {
			Node node = cmd.getSite();
			success = getLauncher().push(node);
		}

		Logger.debug(this, "launch", success, "push %s", cmd.getSite());

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