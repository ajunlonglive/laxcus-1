/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import com.laxcus.command.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.hash.*;

/**
 * HASH站点调用器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public abstract class HashInvoker extends EchoInvoker {

	/**
	 * 构造HASH站点命令调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected HashInvoker(Command cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public HashLauncher getLauncher() {
		return (HashLauncher) super.getLauncher();
	}

}