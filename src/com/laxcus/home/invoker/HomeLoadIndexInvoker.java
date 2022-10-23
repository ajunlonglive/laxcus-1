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
 * 加载索引调用器。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class HomeLoadIndexInvoker extends HomeFastMassInvoker {

	/**
	 * 构造加载索引调用器，指定命令
	 * @param cmd 加载索引命令
	 */
	public HomeLoadIndexInvoker(LoadIndex cmd) {
		super(cmd);
	}

}