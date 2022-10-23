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
 * 代理INSERT命令钩子
 * 
 * @author scott.liang
 * @version 1.0 9/14/2017
 * @since laxcus 1.0
 */
public class TrustInsertHook extends CommandHook {

	/**
	 * 构造默认的代理INSERT命令钩子
	 */
	public TrustInsertHook() {
		super();
	}

	/**
	 * 返回插入行数
	 * @return 有效返回指定值，弹出错误
	 */
	public int getRows() {
		Object e = super.getResult();
		if (e != null && e.getClass() == java.lang.Integer.class) {
			return ((java.lang.Integer) e).intValue();
		}
		throw new IllegalStateException("illegal result");
	}
}
