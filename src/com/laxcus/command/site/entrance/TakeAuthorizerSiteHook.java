/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.entrance;

import com.laxcus.command.*;

/**
 * 获得授权人账号注册地址钩子
 * 
 * @author scott.liang
 * @version 1.0 5/31/2018
 * @since laxcus 1.0
 */
public class TakeAuthorizerSiteHook extends CommandHook {

	/**
	 * 构造默认的获得授权人账号注册地址钩子
	 */
	public TakeAuthorizerSiteHook() {
		super();
	}

	/**
	 * 返回账号结果报告 
	 * @return 实例结果
	 */
	public TakeAuthorizerSiteProduct getProduct() {
		return (TakeAuthorizerSiteProduct) super.getResult();
	}

}