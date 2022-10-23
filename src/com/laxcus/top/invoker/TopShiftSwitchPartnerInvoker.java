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

/**
 * “SWITCH PARTNER”本地转发命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class TopShiftSwitchPartnerInvoker extends TopInvoker {

	/**
	 * 构造“SWITCH PARTNER”本地转发命令调用器，指定命令
	 * @param cmd
	 */
	public TopShiftSwitchPartnerInvoker(ShiftSwitchPartner cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSwitchPartner getCommand() {
		return (ShiftSwitchPartner) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftSwitchPartner shift = getCommand();
		Node endpoint = shift.getRemote();

		SwitchPartner cmd = shift.getCommand();

		// 投递到其它TOP监视站点
		boolean success = directTo(endpoint, cmd);

		Logger.debug(this, "launch", success, "direct to %s", endpoint);

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
