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
 * FindStubSite命令钩子
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class FindStubSiteHook extends CommandHook {

	/**
	 * 构造FindStubSite命令钩子
	 */
	public FindStubSiteHook() {
		super();
	}

	/**
	 * 返回命令处理结果
	 * @return FindStubSiteProduct实例，或者空指针
	 */
	public FindStubSiteProduct getStubSiteProduct() {
		return (FindStubSiteProduct) super.getResult();
	}
}
