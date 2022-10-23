/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.fast.*;

/**
 * “数据索引卸载”命令的异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since 1.0
 */
public class DesktopStopIndexInvoker extends DesktopFastMassInvoker {

	/**
	 * 构造“数据索引卸载”命令的异步调用器，指定数据索引卸载命令
	 * @param cmd
	 */
	public DesktopStopIndexInvoker(StopIndex cmd) {
		super(cmd);
	}

}