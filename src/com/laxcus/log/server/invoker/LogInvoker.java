/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.server.invoker;

import com.laxcus.command.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.log.server.*;

/**
 * LOG站点调用器
 * 
 * @author scott.liang
 * @version 1.0 12/23/2011
 * @since laxcus 1.0
 */
public abstract class LogInvoker extends EchoInvoker {

	/**
	 * 构造LOG站点命令调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected LogInvoker(Command cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public LogLauncher getLauncher() {
		return (LogLauncher) super.getLauncher();
	}

}