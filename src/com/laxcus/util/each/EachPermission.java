/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.each;

import java.security.*;

/**
 * EACH算法签名许可。防止在没有授权的情况下被非法调用。
 * 
 * @author scott.liang
 * @version 1.0 1/5/2009
 * @since laxcus 1.0
 */
public class EachPermission extends BasicPermission {

	private static final long serialVersionUID = 3385021234031139395L;

	/**
	 * 构造签名操作许可
	 * @param name 目标名称
	 */
	public EachPermission(String name) {
		super(name);
	}

	/**
	 * 构造签名操作许可
	 * @param name  目标名称
	 * @param actions 操作行为
	 */
	public EachPermission(String name, String actions) {
		super(name, actions);
	}

}