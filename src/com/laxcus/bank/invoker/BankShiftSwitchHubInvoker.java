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
import com.laxcus.site.Node;

/**
 * 转发切换管理节点命令调用器
 * 
 * @author scott.liang
 * @version 1.0 6/26/2018
 * @since laxcus 1.0
 */
public class BankShiftSwitchHubInvoker extends BankInvoker {

	/**
	 * 构造转发切换管理节点命令调用器，指定命令
	 * @param cmd 转发切换管理节点命令
	 */
	public BankShiftSwitchHubInvoker(ShiftSwitchHub cmd) {
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