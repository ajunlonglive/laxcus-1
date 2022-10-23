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
import com.laxcus.site.*;

/**
 * “SWITCH HUB”本地转发命令调用器。<br>
 * 通知目标地址，切换注册站点。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class TopShiftSwitchHubInvoker extends TopInvoker {

	/**
	 * 构造“SWITCH HUB”本地转发命令调用器，指定命令
	 * @param cmd 本地转发命令
	 */
	public TopShiftSwitchHubInvoker(ShiftSwitchHub cmd) {
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
		// TODO Auto-generated method stub
		return false;
	}

}
