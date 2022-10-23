/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 切换管理站点调用器。<br>
 * 当管理运行站点发生状态，命令由管理站点（TOP/HOME）的监视器站点发出，要求下属用户注册到它的下面。
 * 这是一个单向命令，不需要反馈给请求端。
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class CommonSwitchHubInvoker extends CommonInvoker {

	/**
	 * 构造切换管理站点命令调用器，指定命令。
	 * @param cmd 切换管理站点命令
	 */
	public CommonSwitchHubInvoker(SwitchHub cmd) {
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

		// 切换到新的注册地址
		boolean success = getLauncher().switchHub(hub);
		Logger.debug(this, "launch", success, "switch to %s", hub);

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