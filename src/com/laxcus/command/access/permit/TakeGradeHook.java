/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.command.*;

/**
 * 获取注册账号的操作权级钩子
 * 
 * @author scott.liang
 * @version 1.0 5/31/2018
 * @since laxcus 1.0
 */
public class TakeGradeHook extends CommandHook {

	/**
	 * 构造默认的获取注册账号的操作权级钩子
	 */
	public TakeGradeHook() {
		super();
	}

	/**
	 * 返回账号结果报告 
	 * @return 实例结果
	 */
	public TakeGradeProduct getProduct() {
		return (TakeGradeProduct) super.getResult();
	}

}