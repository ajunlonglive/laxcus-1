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
public class SetReflexDataHook extends CommandHook {

	/**
	 * 构造默认的设置映像数据钩子
	 */
	public SetReflexDataHook() {
		super();
	}

	/**
	 * 返回设置映像数据报告
	 * @return SetReflexDataProduct实例
	 */
	public SetReflexDataProduct getProduct() {
		return (SetReflexDataProduct) super.getResult();
	}
}