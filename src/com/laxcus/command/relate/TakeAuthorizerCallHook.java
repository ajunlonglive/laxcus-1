/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import com.laxcus.command.*;

/**
 * 获得授权人的CALL站点钩子
 * 
 * @author scott.liang
 * @version 1.0 5/31/2018
 * @since laxcus 1.0
 */
public class TakeAuthorizerCallHook extends CommandHook {

	/**
	 * 构造默认的获得授权人的CALL站点钩子
	 */
	public TakeAuthorizerCallHook() {
		super();
	}

	/**
	 * 返回账号结果报告 
	 * @return 实例结果
	 */
	public TakeAuthorizerCallProduct getProduct() {
		return (TakeAuthorizerCallProduct) super.getResult();
	}

}