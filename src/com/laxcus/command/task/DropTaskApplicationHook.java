/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;

/**
 * 删除分布应用命令钩子
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class DropTaskApplicationHook extends CommandHook {

	/**
	 * 构造默认的删除分布应用命令钩子
	 */
	public DropTaskApplicationHook() {
		super();
	}

	/**
	 * 返回发布结果
	 * @return DropTaskApplicationProduct实例
	 */
	public DropTaskApplicationProduct getProduct() {
		return (DropTaskApplicationProduct) super.getResult();
	}
}