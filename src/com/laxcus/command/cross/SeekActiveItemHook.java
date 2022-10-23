/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.command.*;

/**
 * 查询授权单元命令钩子
 * 
 * @author scott.liang
 * @version 1.0 8/15/2017
 * @since laxcus 1.0
 */
public class SeekActiveItemHook extends CommandHook {

	/**
	 * 构造查询授权单元命令钩子
	 */
	public SeekActiveItemHook() {
		super();
	}

	/**
	 * 返回查询结果
	 * @return 返回SeekActiveItemProduct实例，或者空指针
	 */
	public SeekActiveItemProduct getProduct() {
		return (SeekActiveItemProduct) super.getResult();
	}
}