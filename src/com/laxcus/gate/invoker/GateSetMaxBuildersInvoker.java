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
 * 账号BUILD节点数目调用器
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class GateSetMaxBuildersInvoker extends GateSetMultiUserParameterInvoker {

	/**
	 * 构造BUILD节点数目调用器，指定命令
	 * 
	 * @param cmd BUILD节点数目
	 */
	public GateSetMaxBuildersInvoker(SetMaxBuilders cmd) {
		super(cmd);
	}

}