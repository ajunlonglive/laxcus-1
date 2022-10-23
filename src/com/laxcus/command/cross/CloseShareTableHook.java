/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.command.*;

/**
 * 关闭授权表钩子
 * 
 * @author scott.liang
 * @version 1.0 7/21/2018
 * @since laxcus 1.0
 */
public class CloseShareTableHook extends CommandHook {

	/**
	 * 构造关闭授权表钩子
	 */
	public CloseShareTableHook() {
		super();
	}

	/**
	 * 返回关闭的授权单元结果
	 * @return 授权单元实例，或者空指针
	 */
	public ShareCrossProduct getProduct() {
		return (ShareCrossProduct) super.getResult();
	}

}