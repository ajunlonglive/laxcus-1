/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.*;

/**
 * SQL查询命令调用器 <br>
 * 
 * 它是SELECT、DELETE、UPDATE调用器的父类。
 * 
 * @author scott.liang
 * @version 1.0 05/29/2021
 * @since laxcus 1.0
 */
public abstract class DesktopQueryInvoker extends DesktopInvoker {

	/**
	 * 构造默认的SQL查询调用器，指定命令
	 * @param cmd SQL查询命令
	 */
	protected DesktopQueryInvoker(Query cmd) {
		super(cmd);
	}

}