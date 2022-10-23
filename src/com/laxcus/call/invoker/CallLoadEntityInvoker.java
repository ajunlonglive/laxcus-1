/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.access.fast.*;

/**
 * “加载数据块”命令调用器。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class CallLoadEntityInvoker extends CallFastMassInvoker {

	/**
	 * 构造“加载数据块”命令调用顺
	 * @param cmd 加载数据块命令
	 */
	public CallLoadEntityInvoker(LoadEntity cmd) {
		super(cmd);
	}

}