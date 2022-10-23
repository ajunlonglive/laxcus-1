/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;

/**
 * 通用命令调用器。<br>
 * 
 * 通用命令调用器具有跨站点的性质，在不同站点下通用。
 * 
 * @author scott.liang
 * @version 1.0 04/23/2013
 * @since laxcus 1.0
 */
public abstract class CommonInvoker extends EchoInvoker {

	/**
	 * 构造通用命令调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected CommonInvoker(Command cmd) {
		super(cmd);
	}

}