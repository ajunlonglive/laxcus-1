/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.access.fast.*;

/**
 * 卸载数据块调用器。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class HomeStopEntityInvoker extends HomeFastMassInvoker {

	/**
	 * 构造卸载数据块调用器，指定命令
	 * @param cmd 卸载数据块命令
	 */
	public HomeStopEntityInvoker(StopEntity cmd) {
		super(cmd);
	}

}