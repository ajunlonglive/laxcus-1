/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.command.*;

/**
 * 请求资源引用钩子。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/2/2013
 * @since laxcus 1.0
 */
public class RequestReferHook extends CommandHook {
	
	/**
	 * 构造请求资源引用钩子
	 */
	public RequestReferHook() {
		super();
	}

	/**
	 * 取出分配结果
	 * @return 返回分配的资源引用结果，或者空指针
	 */
	public RequestReferProduct getRequestReferProduct() {
		return (RequestReferProduct) super.getResult();
	}

}