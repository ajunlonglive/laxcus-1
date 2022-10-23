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
 * 建立数据库命令钩子
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class CreateSchemaHook extends CommandHook {

	/**
	 * 构造默认的建立数据库命令钩子
	 */
	public CreateSchemaHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return CreateSchemaProduct实例
	 */
	public CreateSchemaProduct getProduct() {
		return (CreateSchemaProduct) super.getResult();
	}

}