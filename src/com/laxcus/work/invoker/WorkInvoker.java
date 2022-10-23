/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.command.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.work.*;

/**
 * WORK站点调用器
 * 
 * @author scott.liang
 * @version 1.0 4/23/2011
 * @since laxcus 1.0
 */
public abstract class WorkInvoker extends EchoInvoker {

	/**
	 * 构造WORK站点命令调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected WorkInvoker(Command cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public WorkLauncher getLauncher() {
		return (WorkLauncher) super.getLauncher();
	}

}