/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import com.laxcus.command.*;

/**
 * INSERT命令钩子
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class InsertHook extends CommandHook {

	/**
	 * 构造INSERT命令钩子
	 */
	public InsertHook() {
		super();
	}

	/**
	 * 返回数据结果
	 * @return AssumeInsert实例
	 */
	public AssumeInsert getProduct() {
		return (AssumeInsert) super.getResult();
	}
}