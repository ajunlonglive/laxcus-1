/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import com.laxcus.command.*;

/**
 * FindSpacePrimeSite命令钩子
 * 
 * @author scott.liang
 * @version 1.0 06/22/2013
 * @since laxcus 1.0
 */
public class FindSpacePrimeSiteHook extends CommandHook {

	/**
	 * 构造默认的FindSpacePrimeSite命令钩子
	 */
	public FindSpacePrimeSiteHook() {
		super();
	}

	/**
	 * 返回处理结果
	 * @return FindSpacePrimeSiteProduct实例，或者空指针
	 */
	public FindSpacePrimeSiteProduct getProduct() {
		return (FindSpacePrimeSiteProduct) super.getResult();
	}
}
