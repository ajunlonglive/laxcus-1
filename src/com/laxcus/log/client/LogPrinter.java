/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

/**
 * 本地日志打印输出接口
 * 
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public interface LogPrinter {

	/**
	 * 显示日志
	 * @param e
	 */
	void print(String e);
}
