/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.user.*;

/**
 * 中间缓存尺寸调用器
 * 
 * @author scott.liang
 * @version 1.0 1/10/2021
 * @since laxcus 1.0
 */
public class GateSetMiddleBufferInvoker extends GateSetMultiUserParameterInvoker {

	/**
	 * 构造中间缓存尺寸调用器，指定命令
	 * 
	 * @param cmd 中间缓存尺寸
	 */
	public GateSetMiddleBufferInvoker(SetMiddleBuffer cmd) {
		super(cmd);
	}

}