/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.find;

import com.laxcus.command.*;

/**
 * “FindUserSite”命令钩子
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class FindUserSiteHook extends CommandHook {

	/**
	 * 构造默认的“FindUserSite”命令钩子
	 */
	public FindUserSiteHook() {
		super();
	}

	/**
	 * 返回处理结果
	 * @return FindUserSiteProduct实例
	 */
	public FindUserSiteProduct getProduct() {
		return (FindUserSiteProduct) super.getResult();
	}

}