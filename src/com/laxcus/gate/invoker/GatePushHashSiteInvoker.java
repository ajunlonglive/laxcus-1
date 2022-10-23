/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.site.bank.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;

/**
 * 推送HASH站点到GATE站点调用器
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class GatePushHashSiteInvoker extends GateInvoker {

	/**
	 * 构造推送HASH站点到GATE站点，指定命令
	 * @param cmd 推送HASH站点到GATE站点
	 */
	public GatePushHashSiteInvoker(PushHashSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushHashSite getCommand() {
		return (PushHashSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PushHashSite cmd = getCommand();
		boolean success = StaffOnGatePool.getInstance().add(cmd.getNo(), cmd.getNode());

		Logger.debug(this, "launch", success, "add %s#%d", cmd.getNode(), cmd.getNo());

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