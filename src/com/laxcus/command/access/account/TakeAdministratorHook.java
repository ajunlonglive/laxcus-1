/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;

/**
 * 获取系统管理员账号钩子
 * 
 * @author scott.liang
 * @version 1.0 7/28/2018
 * @since laxcus 1.0
 */
public class TakeAdministratorHook extends CommandHook {

	/**
	 * 构造默认的获取系统管理员账号钩子
	 */
	public TakeAdministratorHook() {
		super();
	}

	/**
	 * 返回系统管理员账号结果报告 
	 * @return
	 */
	public TakeAdministratorProduct getProduct() {
		return (TakeAdministratorProduct) super.getResult();
	}
	
	/**
	 * 取出系统管理员账号
	 * @return 返回系统管理员账号实例，或者空指针
	 */
	public Administrator getAdministrator() {
		TakeAdministratorProduct product = getProduct();
		return (product != null ? product.getAdministrator() : null);
	}

}