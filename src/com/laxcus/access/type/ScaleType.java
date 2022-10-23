/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

/**
 * 码位计算器类型
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public class ScaleType {

	/** 码位计算标识 **/
	public final static byte SHORT_SCALE = 1;
	public final static byte INTEGER_SCALE = 2;
	public final static byte LONG_SCALE = 3;
	public final static byte FLOAT_SCALE = 4;
	public final static byte DOUBLE_SCALE = 5;

	/**
	 * 判断是有效的类型 
	 * @param who  被判断值
	 * @return  返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case ScaleType.SHORT_SCALE:
		case ScaleType.INTEGER_SCALE:
		case ScaleType.LONG_SCALE:
		case ScaleType.FLOAT_SCALE:
		case ScaleType.DOUBLE_SCALE:
			return true;
		default:
			return false;
		}
	}

}
