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
 * 账号单表最大索引数目调用器
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class GateSetMaxIndexesInvoker extends GateSetMultiUserParameterInvoker {

	/**
	 * 构造单表最大索引数目调用器，指定命令
	 * 
	 * @param cmd 单表最大索引数目
	 */
	public GateSetMaxIndexesInvoker(SetMaxIndexes cmd) {
		super(cmd);
	}

}