/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

/**
 * 检索索引类型 <br>
 * 
 * 用于SQL WHERE检索
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public class IndexType {

	/** SQL WHERE 检索时规定的KEY类型标识，见 com.laxcus.access.index  **/
	
	public final static byte SHORT_INDEX = 1;

	public final static byte INTEGER_INDEX = 2;

	public final static byte LONG_INDEX = 3;

	public final static byte FLOAT_INDEX = 4;

	public final static byte DOUBLE_INDEX = 5;

	public final static byte NESTED_INDEX = 6;

	public final static byte ON_INDEX = 7;
	
	public final static byte DOCK_INDEX = 8;

	/**
	 * 判断是合法的检索索引类型
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case IndexType.SHORT_INDEX:
		case IndexType.INTEGER_INDEX:
		case IndexType.LONG_INDEX:
		case IndexType.FLOAT_INDEX:
		case IndexType.DOUBLE_INDEX:
		case IndexType.NESTED_INDEX:
		case IndexType.ON_INDEX:
		case IndexType.DOCK_INDEX:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是SHORT检索索引类型
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isShortIndex(byte who) {
		return who == IndexType.SHORT_INDEX;
	}

	/**
	 * 判断是INTEGER检索索引类型
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isIntegerIndex(byte who) {
		return who == IndexType.INTEGER_INDEX;
	}

	/**
	 * 判断是LONG检索索引类型
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isLongIndex(byte who) {
		return who == IndexType.LONG_INDEX;
	}

	/**
	 * 判断是FLOAT检索索引类型
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isFloatIndex(byte who) {
		return who == IndexType.FLOAT_INDEX;
	}

	/**
	 * 判断是DOUBLE检索索引类型
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isDoubleIndex(byte who) {
		return who == IndexType.DOUBLE_INDEX;
	}

	/**
	 * 判断嵌套索引
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isNestedIndex(byte who) {
		return who == IndexType.NESTED_INDEX;
	}
	
	/**
	 * 判断是ON索引
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isOnIndex(byte who) {
		return who == IndexType.ON_INDEX;
	}

	/**
	 * 判断是DOCK索引
	 * @param who 检索索引类型
	 * @return 返回真或者假
	 */
	public static boolean isDockIndex(byte who) {
		return who == IndexType.DOCK_INDEX;
	}
}