/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.trust;

import com.laxcus.command.*;

/**
 * 代理DELETE命令钩子
 * 
 * @author scott.liang
 * @version 1.0 9/14/2017
 * @since laxcus 1.0
 */
public class TrustDeleteHook extends CommandHook {

	/**
	 * 构造默认的代理DELETE命令钩子
	 */
	public TrustDeleteHook() {
		super();
	}

	/**
	 * 返回删除行数。三种情况：<br>
	 * 1. 大于0是删除行数；2. 等于0是没有找到；3. 小于0是错误码。
	 * 
	 * @return 删除行数或者错误码
	 */
	public int getRows() {
		Object e = super.getResult();
		if (e != null && e.getClass() == java.lang.Integer.class) {
			return ((java.lang.Integer) e).intValue();
		}
		throw new IllegalStateException("illegal result");
	}
}