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
 * 账号最多应用软件数目调用器
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class GateSetMaxTasksInvoker extends GateSetMultiUserParameterInvoker {

	/**
	 * 构造最多应用软件数目调用器，指定命令
	 * 
	 * @param cmd 最多应用软件数目
	 */
	public GateSetMaxTasksInvoker(SetMaxTasks cmd) {
		super(cmd);
	}

}