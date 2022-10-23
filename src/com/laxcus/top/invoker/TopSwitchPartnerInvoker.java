/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.top.*;

/**
 * “SWITCH PARTNER”命令调用器。
 * 新的TOP管理站点要求监视站点，注册到它的下面。
 * 
 * @author scott.liang
 * @version 1.0 4/23/2014
 * @since laxcus 1.0
 */
public class TopSwitchPartnerInvoker extends TopInvoker {

	/**
	 * 构造“SWITCH PARTNER”命令调用器，指定命令
	 * @param cmd “SWITCH PARTNER”命令
	 */
	public TopSwitchPartnerInvoker(SwitchPartner cmd) {
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
		boolean success = TopLauncher.getInstance().isMonitor();
		if (success) {
			// 注册到新的管理站点
			success = TopMonitor.getInstance().switchHub(hub);
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
