/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.login;

import com.laxcus.command.*;

/**
 * 注册站点命令钩子
 * 
 * @author scott.liang
 * @version 1.0 12/4/2017
 * @since laxcus 1.0
 */
public class LoginSiteHook extends CommandHook {

	/**
	 * 构造默认的注册站点命令钩子
	 */
	public LoginSiteHook() {
		super();
	}

	/**
	 * 判断处理成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		LoginSiteProduct product = (LoginSiteProduct) super.getResult();
		return (product != null && product.isSuccessful());
	}
}
