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
 * 删除账号钩子
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public class DropUserHook extends CommandHook {

	/**
	 * 构造默认的删除账号钩子
	 */
	public DropUserHook() {
		super();
	}

	/**
	 * 返回删除账号报告 
	 * @return DropUserProduct
	 */
	public DropUserProduct getProduct() {
		return (DropUserProduct) super.getResult();
	}

}