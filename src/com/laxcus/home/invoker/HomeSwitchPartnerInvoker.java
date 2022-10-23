/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.site.*;
import com.laxcus.home.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 切换管理节点命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/23/2014
 * @since laxcus 1.0
 */
public class HomeSwitchPartnerInvoker extends HomeInvoker {

	/**
	 * 构造切换管理节点命令调用器，指定命令
	 * @param cmd - 切换管理节点命令
	 */
	public HomeSwitchPartnerInvoker(SwitchPartner cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SwitchPartner getCommand() {
		return (SwitchPartner) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SwitchPartner cmd = getCommand();
		Node hub = cmd.getHub();

		// 当前站点是监视站点
		boolean success = HomeLauncher.getInstance().isMonitor();
		// 注册到新的管理站点
		if (success) {
			success = HomeMonitor.getInstance().switchHub(hub);
		}

		Logger.debug(this, "launch", success, "switch to %s", hub);

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
