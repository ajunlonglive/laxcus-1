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
 * 判断数据表存在命令钩子
 * 
 * @author scott.liang
 * @version 1.1 8/18/2015
 * @since laxcus 1.0
 */
public class AssertTableHook extends CommandHook {

	/**
	 * 构造默认的判断数据表存在命令钩子
	 */
	public AssertTableHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return AssertTableProduct实例
	 */
	public AssertTableProduct getProduct() {
		return (AssertTableProduct) super.getResult();
	}

}