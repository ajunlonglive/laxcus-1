/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.parameter;

/**
 * 引导参数类型
 * 
 * @author scott.liang
 * @version 1.0 7/25/2020
 * @since laxcus 1.0
 */
public final class InputParameterType {

	/** 引导参数数据类型  **/
	public final static byte BOOLEAN = 1;
	public final static byte STRING = 2;

	/** 整数 **/
	public final static byte SHORT = 3;
	public final static byte INTEGER = 4;
	public final static byte LONG = 5;

	/** 浮点数 **/
	public final static byte FLOAT = 6;
	public final static byte DOUBLE = 7;

	/** 时间/日期 **/
	public final static byte DATE = 8;
	public final static byte TIME = 9;
	public final static byte TIMESTAMP = 10;

	/**
	 * 判断是引导参数类型
	 * @param who 引导参数类型
	 * @return 返回“真”或者“假”
	 */
	public static boolean isValueType(byte who) {
		switch (who) {
		case InputParameterType.BOOLEAN:
		case InputParameterType.STRING:
		case InputParameterType.SHORT:
		case InputParameterType.INTEGER:
		case InputParameterType.LONG:
		case InputParameterType.FLOAT:
		case InputParameterType.DOUBLE:
		case InputParameterType.DATE:
		case InputParameterType.TIME:
		case InputParameterType.TIMESTAMP:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是引导参数类型
	 * @param who 引导参数类型
	 * @return 返回“真”或者“假”
	 */
	public static String translate(byte who) {
		switch (who) {
		case InputParameterType.BOOLEAN:
			return "bool";
		case InputParameterType.STRING:
			return "string";
		case InputParameterType.SHORT:
			return "short";
		case InputParameterType.INTEGER:
			return "int";
		case InputParameterType.LONG:
			return "long";
		case InputParameterType.FLOAT:
			return "float";
		case InputParameterType.DOUBLE:
			return "double";
		case InputParameterType.DATE:
			return "date";
		case InputParameterType.TIME:
			return "time";
		case InputParameterType.TIMESTAMP:
			return "timestamp";
		default:
			return "none";
		}
	}
	
	/**
	 * 判断是布尔类型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isBoolean(byte who) {
		return who == InputParameterType.BOOLEAN;
	}

	/**
	 * 判断是字符串类型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isString(byte who) {
		return who == InputParameterType.STRING;
	}

	/**
	 * 判断是短整型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isShort(byte who) {
		return who == InputParameterType.SHORT;
	}

	/**
	 * 判断是整型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isInteger(byte who) {
		return who == InputParameterType.INTEGER;
	}

	/**
	 * 判断是长整型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isLong(byte who) {
		return who == InputParameterType.LONG;
	}

	/**
	 * 判断是单浮点值
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isFloat(byte who) {
		return who == InputParameterType.FLOAT;
	}

	/**
	 * 判断是双浮点值
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isDouble(byte who) {
		return who == InputParameterType.DOUBLE;
	}

	/**
	 * 判断是日期类型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isDate(byte who) {
		return who == InputParameterType.DATE;
	}

	/**
	 * 判断是时间类型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isTime(byte who) {
		return who == InputParameterType.TIME;
	}

	/**
	 * 判断是时间戳类型
	 * @param who 引导参数类型
	 * @return 返回真或者假
	 */
	public static boolean isTimestamp(byte who) {
		return who == InputParameterType.TIMESTAMP;
	}

}