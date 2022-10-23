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
 * 向BANK站点申请主机序列号钩子
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeSiteSerialHook extends CommandHook {

	/**
	 * 构造默认的向BANK站点申请主机序列号钩子
	 */
	public TakeSiteSerialHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return 申请主机序列号BANK站点反馈结果
	 */
	public TakeSiteSerialProduct getProduct() {
		return (TakeSiteSerialProduct) super.getResult();
	}

}