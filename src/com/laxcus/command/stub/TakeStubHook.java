/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub;

import com.laxcus.command.*;

/**
 * 数据块编号钩子
 * 
 * @author scott.liang
 * @version 1.0 03/04/2012
 * @since laxcus 1.0
 */
public final class TakeStubHook extends CommandHook {

	/**
	 * 构造默认的数据块编号钩子
	 */
	public TakeStubHook() {
		super();
	}

	/**
	 * 返回数据块编号集
	 * @return StubProduct实例，或者空指针
	 */
	public StubProduct getStubProduct() {
		return (StubProduct) super.getResult();
	}
	
}
