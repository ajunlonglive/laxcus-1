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
 * 用户命令权级调用器
 * 
 * @author scott.liang
 * @version 1.0 10/6/2022
 * @since laxcus 1.0
 */
public class GateSetUserPriorityInvoker extends GateSetMultiUserParameterInvoker {

	/**
	 * 构造用户命令权级调用器，指定命令
	 * 
	 * @param cmd 用户命令权级
	 */
	public GateSetUserPriorityInvoker(SetUserPriority cmd) {
		super(cmd);
	}

}