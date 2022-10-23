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
 * 设置映像数据钩子
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public class DeleteCacheReflexHook extends CommandHook {

	/**
	 * 构造默认的设置映像数据钩子
	 */
	public DeleteCacheReflexHook() {
		super();
	}

	/**
	 * 返回更新数据块报告
	 * @return DeleteCacheReflexProduct实例
	 */
	public DeleteCacheReflexProduct getProduct() {
		return (DeleteCacheReflexProduct) super.getResult();
	}
}