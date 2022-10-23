/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * “SWITCH HUB”本地转发命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class HomeShiftSwitchHubInvoker extends HomeInvoker {

	/**
	 * 构造“SWITCH HUB”本地转发命令调用器，指定命令
	 * @param cmd
	 */
	public HomeShiftSwitchHubInvoker(ShiftSwitchHub cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSwitchHub getCommand() {
		return (ShiftSwitchHub) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftSwitchHub shift = getCommand();
		Node slave = shift.getRemote();
		SwitchHub cmd = shift.getCommand();

		// 投递到目标站点，不需要反馈
		boolean success = directTo(slave, cmd);

		Logger.debug(this, "launch", success, "direct to %s", slave);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}
