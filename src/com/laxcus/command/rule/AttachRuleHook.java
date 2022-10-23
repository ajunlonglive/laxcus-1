/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rule;

import com.laxcus.command.*;

/**
 * 转发绑定规则命令钩子
 * 
 * @author scott.liang
 * @version 1.0 4/29/2018
 * @since laxcus 1.0
 */
public class AttachRuleHook extends CommandHook {

	/**
	 * 构造默认的转发绑定规则命令钩子
	 */
	public AttachRuleHook() {
		super();
	}

	/**
	 * 判断命令转发成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		Object obj = super.getResult();
		if (obj != null && obj.getClass() == java.lang.Boolean.class) {
			Boolean b = (Boolean) obj;
			return b.booleanValue();
		}
		return false;
	}

}