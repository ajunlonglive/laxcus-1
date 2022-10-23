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
 * DELETE命令钩子
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class DeleteHook extends CommandHook {

	/**
	 * 构造DELETE命令钩子
	 */
	public DeleteHook() {
		super();
	}

	/**
	 * 返回结果
	 * @return AssumeDelete
	 */
	public AssumeDelete getProduct() {
		return (AssumeDelete) super.getResult();
	}
}