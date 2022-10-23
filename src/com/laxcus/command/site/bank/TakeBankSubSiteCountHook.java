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
 * 获得BANK子站点数目钩子
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeBankSubSiteCountHook extends CommandHook {

	/**
	 * 构造默认的获得BANK子站点数目钩子
	 */
	public TakeBankSubSiteCountHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return 获得BANK子站点数目反馈结果
	 */
	public TakeBankSubSiteCountProduct getProduct() {
		return (TakeBankSubSiteCountProduct) super.getResult();
	}

}