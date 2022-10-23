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
 * “数据块加载”命令的异步调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopLoadEntityInvoker extends DesktopFastMassInvoker {

	/**
	 * 构造“数据块加载”命令的异步调用器，指定数据块加载命令
	 * @param cmd
	 */
	public DesktopLoadEntityInvoker(LoadEntity cmd) {
		super(cmd);
	}


}