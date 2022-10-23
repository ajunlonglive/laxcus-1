/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.command.*;

/**
 * 查询映像数据块站点命令钩子
 * 
 * @author scott.liang
 * @version 1.0 11/2/2013
 * @since laxcus 1.0
 */
public class FindReflexStubSiteHook extends CommandHook {

	/**
	 * 构造默认的查询映像数据块站点钩子
	 */
	public FindReflexStubSiteHook() {
		super();
	}

	/**
	 * 返回映像数据块站点报告
	 * @return ReflexStubSiteProduct实例
	 */
	public ReflexStubSiteProduct getProduct() {
		return (ReflexStubSiteProduct) super.getResult();
	}
}
