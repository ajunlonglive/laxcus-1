/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;

/**
 * 获得账号资源引用钩子
 * 
 * @author scott.liang
 * @version 1.0 7/30/2017
 * @since laxcus 1.0
 */
public class TakeReferHook extends CommandHook {

	/**
	 * 构造获得账号资源引用钩子
	 */
	public TakeReferHook() {
		super();
	}
	
	/**
	 * 返回资源引用结果
	 * @return
	 */
	public TakeReferProduct getProduct() {
		return (TakeReferProduct) super.getResult();
	}

	/**
	 * 返回账号资源引用实例
	 * @return 返回Refer实例，或者空指针
	 */
	public Refer getRefer() {
		TakeReferProduct product = getProduct();
		if (product != null) {
			return product.getRefer();
		}
		return null;
	}
}