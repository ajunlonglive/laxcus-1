/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.command.*;

/**
 * 删除账号钩子
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public class DropTableHook extends CommandHook {

	/**
	 * 构造默认的删除账号钩子
	 */
	public DropTableHook() {
		super();
	}

	/**
	 * 返回账号结果报告 
	 * @return
	 */
	public DropTableProduct getProduct() {
		return (DropTableProduct) super.getResult();
	}

}