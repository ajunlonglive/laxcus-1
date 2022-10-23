/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

/**
 * 边缘容器代理
 * 
 * @author scott.liang
 * @version 1.0 6/19/2019
 * @since laxcus 1.0
 */
public interface TubTrustor {

	/**
	 * 解除与代理的绑定
	 * @param tub 任务实例
	 * @throws TubException
	 */
	boolean detach(TubServlet that);
}