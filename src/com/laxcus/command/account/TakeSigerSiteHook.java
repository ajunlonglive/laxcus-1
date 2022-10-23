/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.account;

import com.laxcus.command.*;

/**
 * 获得签名的ACCOUNT站点的命令钩子
 * 
 * @author scott.liang
 * @version 1.1 7/28/2018
 * @since laxcus 1.0
 */
public class TakeSigerSiteHook extends CommandHook {

	/**
	 * 构造获得签名的ACCOUNT站点的命令钩子
	 */
	public TakeSigerSiteHook() {
		super();
	}

	/**
	 * 输出处理结果
	 * @return 返回获得签名的ACCOUNT站点处理结果实例
	 */
	public TakeSigerSiteProduct getProduct() {
		return (TakeSigerSiteProduct) super.getResult();
	}
}
