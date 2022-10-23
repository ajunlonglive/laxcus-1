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
 * “加载索引”调用器。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class CallLoadIndexInvoker extends CallFastMassInvoker {

	/**
	 * 构造“加载索引命令”调用器
	 * @param cmd
	 */
	public CallLoadIndexInvoker(LoadIndex cmd) {
		super(cmd);
	}

}
