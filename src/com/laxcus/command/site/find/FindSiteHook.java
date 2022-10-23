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
 * “FindSite”命令钩子
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class FindSiteHook extends CommandHook {

	/**
	 * 构造默认的“FindSite”命令钩子
	 */
	public FindSiteHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return FindSiteProduct实例
	 */
	public FindSiteProduct getProduct() {
		return (FindSiteProduct) super.getResult();
	}

}
