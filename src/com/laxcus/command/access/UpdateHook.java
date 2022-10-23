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
 * UPDATE命令钩子
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class UpdateHook extends CommandHook {

	/**
	 * 构造UPDATE命令钩子
	 */
	public UpdateHook() {
		super();
	}

	/**
	 * 输出UPDATE操作的处理结果
	 * @return 返回AssumeUpdate实例
	 */
	public AssumeUpdate getProduct() {
		return (AssumeUpdate) super.getResult();
	}
}