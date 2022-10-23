/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.command.*;

/**
 * 判断数据库存在命令钩子
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class AssertSchemaHook extends CommandHook {

	/**
	 * 构造默认的判断数据库存在命令钩子
	 */
	public AssertSchemaHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return AssertSchemaProduct实例
	 */
	public AssertSchemaProduct getProduct() {
		return (AssertSchemaProduct) super.getResult();
	}

}