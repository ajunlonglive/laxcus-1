/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.user;

import com.laxcus.command.*;

/**
 * FindFrontLoginSite命令钩子
 * 
 * @author scott.liang
 * @version 1.0 2/15/2017
 * @since laxcus 1.0
 */
public class FindFrontLoginSiteHook extends CommandHook {

	/**
	 * 构造FindFrontLoginSite命令钩子
	 */
	public FindFrontLoginSiteHook() {
		super();
	}

	/**
	 * 返回结果
	 * @return FindFrontLoginSiteProduct实例
	 */
	public FindFrontLoginSiteProduct getProduct() {
		return (FindFrontLoginSiteProduct) super.getResult();
	}
}