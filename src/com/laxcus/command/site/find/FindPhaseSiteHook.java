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
 * “FindPhaseSite”命令钩子
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class FindPhaseSiteHook extends CommandHook {

	/**
	 * 构造默认的“FindPhaseSite”命令钩子
	 */
	public FindPhaseSiteHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return FindPhaseSiteProduct实例
	 */
	public FindPhaseSiteProduct getProduct() {
		return (FindPhaseSiteProduct) super.getResult();
	}

}