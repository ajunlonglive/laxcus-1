/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * 调用器编号
 * 
 * @author scott.liang
 * @version 1.0 3/21/2012
 * @since laxcus 1.0
 */
public final class InvokerIdentity {

	/** 无效编号 **/
	public static final long INVALID = -1L;

	/**
	 * 判断调用器编号有效
	 * @param invokerId 调用器编号
	 * @return 返回真或者假
	 */
	public static boolean isValid(long invokerId) {
		return invokerId >= 0L;
	}

	/**
	 * 判断调用器编号无效
	 * @param invokerId 调用器编号
	 * @return 返回真或者假
	 */
	public static boolean isInvalid(long invokerId) {
		return invokerId < 0L;
	}
}
