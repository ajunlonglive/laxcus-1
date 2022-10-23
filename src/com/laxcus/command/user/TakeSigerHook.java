/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.user;

import com.laxcus.command.*;

/**
 * 申请用户签名命令钩子
 * 
 * @author scott.liang
 * @version 1.0 3/03/2012
 * @since laxcus 1.0
 */
public class TakeSigerHook extends CommandHook {

	/**
	 * 构造申请用户签名命令钩子
	 */
	public TakeSigerHook() {
		super();
	}

	/**
	 * 返回结果
	 * @return TakeSigerProduct实例
	 */
	public TakeSigerProduct getProduct() {
		return (TakeSigerProduct) super.getResult();
	}
}