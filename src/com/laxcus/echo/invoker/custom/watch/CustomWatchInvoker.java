/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom.watch;

import com.laxcus.command.custom.*;
import com.laxcus.watch.invoker.*;

/**
 * WATCH节点的自定义命令调用器，所有在WATCH节点上的第三方操作，都必须从这个类派生。
 * 
 * @author scott.liang
 * @version 1.0 11/2/2017
 * @since laxcus 1.0
 */
public abstract class CustomWatchInvoker extends WatchInvoker {

	/**
	 * 构造 WATCH节点的自定义命令调用器
	 * @param cmd 自定义命令
	 */
	protected CustomWatchInvoker(CustomCommand cmd) {
		super(cmd);
	}

}