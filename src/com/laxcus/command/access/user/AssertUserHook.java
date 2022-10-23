/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.command.*;

/**
 * 判断用户账号存在命令钩子
 * 
 * @author scott.liang
 * @version 1.0 4/12/2010
 * @since laxcus 1.0
 */
public class AssertUserHook extends CommandHook {

	/**
	 * 构造默认的判断用户账号存在命令钩子
	 */
	public AssertUserHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return AssertUserProduct实例
	 */
	public AssertUserProduct getProduct() {
		return (AssertUserProduct) super.getResult();
	}

}