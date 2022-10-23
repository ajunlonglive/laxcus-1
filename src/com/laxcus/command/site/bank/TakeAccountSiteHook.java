/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.command.*;
import com.laxcus.site.*;

/**
 * 查询账号所在的ACCOUNT站点钩子
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeAccountSiteHook extends CommandHook {

	/**
	 * 构造默认的查询账号所在的ACCOUNT站点钩子
	 */
	public TakeAccountSiteHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return 查询账号所在的ACCOUNT站点反馈结果
	 */
	public TakeAccountSiteProduct getProduct() {
		return (TakeAccountSiteProduct) super.getResult();
	}

	/**
	 * 返回ACCOUNT站点
	 * @return ACCOUNT站点
	 */
	public Node getRemote() {
		TakeAccountSiteProduct product = getProduct();
		return (product != null ? product.getRemote() : null);
	}
}