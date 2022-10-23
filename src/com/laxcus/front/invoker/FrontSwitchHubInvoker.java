/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.site.*;
import com.laxcus.front.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * FRONT站点的“SWITCH HUB”命令调用器。
 * SWITCH HUB命令来自GATE站点，通知它切换到新的GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2015
 * @since laxcus 1.0
 */
public class FrontSwitchHubInvoker extends FrontInvoker {

	/**
	 * 构造“SWITCH HUB”命令调用器，指定命令。
	 * @param cmd SWITCH HUB命令
	 */
	public FrontSwitchHubInvoker(SwitchHub cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SwitchHub getCommand() {
		return (SwitchHub) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SwitchHub cmd = getCommand();
		Node hub = cmd.getHub();

		// 更新注册站点地址和重新注册
		FrontLauncher launcher = getLauncher();
		boolean success = launcher.switchHub(hub);

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
