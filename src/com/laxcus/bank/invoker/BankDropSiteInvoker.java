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
 * 推送注销站点命令调用器
 * 
 * @author scott.liang
 * @version 1.0 6/28/2018
 * @since laxcus 1.0
 */
public class BankDropSiteInvoker extends BankInvoker {

	/**
	 * 构造推送注销站点命令调用器，指定命令
	 * @param cmd 推送注销站点命令
	 */
	public BankDropSiteInvoker(DropSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSite getCommand() {
		return (DropSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropSite cmd = getCommand();
		// 判断是监视站点
		boolean success = isMonitor();
		// 保存这个站点
		if (success) {
			Node node = cmd.getSite();
			success = getLauncher().drop(node);
		}

		Logger.debug(this, "launch", success, "drop %s", cmd.getSite());

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