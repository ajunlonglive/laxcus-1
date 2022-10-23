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
 * 用户账号期满调用器
 * 
 * @author scott.liang
 * @version 1.0 1/4/2020
 * @since laxcus 1.0
 */
public class GateSetExpireTimeInvoker extends GateSetMultiUserParameterInvoker {

	/**
	 * 构造BUILD节点数目调用器，指定命令
	 * 
	 * @param cmd BUILD节点数目
	 */
	public GateSetExpireTimeInvoker(SetExpireTime cmd) {
		super(cmd);
	}

}