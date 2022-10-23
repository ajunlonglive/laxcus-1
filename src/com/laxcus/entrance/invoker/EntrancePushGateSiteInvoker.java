/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.invoker;

import com.laxcus.command.site.bank.*;
import com.laxcus.entrance.pool.*;
import com.laxcus.log.client.*;

/**
 * 推送GATE站点到ENTRANCE站点调用器
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class EntrancePushGateSiteInvoker extends EntranceInvoker {

	/**
	 * 构造推送GATE站点到ENTRANCE站点，指定命令
	 * @param cmd 推送GATE站点到ENTRANCE站点
	 */
	public EntrancePushGateSiteInvoker(PushGateSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushGateSite getCommand() {
		return (PushGateSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PushGateSite cmd = getCommand();
		boolean success = StaffOnEntrancePool.getInstance().add(cmd.getInner(),
				cmd.getOuter(), cmd.getNo());

		Logger.debug(this, "launch", success, "add gate site! inner:%s, outer:%s, no:%d",
				cmd.getInner(), cmd.getOuter(), cmd.getNo());

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
