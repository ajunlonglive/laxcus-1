/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;

/**
 * 获得坐标范围内账号钩子
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeAccountSigerHook extends CommandHook {

	/**
	 * 构造默认的获得坐标范围内账号钩子
	 */
	public TakeAccountSigerHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return 获得坐标范围内账号反馈结果
	 */
	public TakeAccountSigerProduct getProduct() {
		return (TakeAccountSigerProduct) super.getResult();
	}

}