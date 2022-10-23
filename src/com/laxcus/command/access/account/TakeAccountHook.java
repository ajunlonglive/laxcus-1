/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;

/**
 * 申请用户账号钩子
 * 
 * @author scott.liang
 * @version 1.1 4/9/2015
 * @since laxcus 1.0
 */
public class TakeAccountHook extends CommandHook {

	/**
	 * 构造默认的申请用户账号钩子
	 */
	public TakeAccountHook() {
		super();
	}

	/**
	 * 返回账号结果报告 
	 * @return
	 */
	public TakeAccountProduct getProduct() {
		return (TakeAccountProduct) super.getResult();
	}
	
	/**
	 * 取出账号
	 * @return 返回账号实例，或者空指针
	 */
	public Account getAccount() {
		TakeAccountProduct product = getProduct();
		return (product != null ? product.getAccount() : null);
	}

}