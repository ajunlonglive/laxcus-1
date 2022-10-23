/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

import com.laxcus.command.*;

/**
 * SeekAttender转发命令钩子
 * 
 * @author scott.liang
 * @version 1.0 3/20/2017
 * @since laxcus 1.0
 */
public class SeekAttenderHook extends CommandHook {

	/**
	 * 构造默认的SeekAttender转发命令钩子
	 */
	public SeekAttenderHook() {
		super();
	}

	/**
	 * 输出处理结果集合
	 * @return 返回SeekAttenderTable实例
	 */
	public SeekAttenderTable getProduct() {
		return (SeekAttenderTable) super.getResult();
	}
}
