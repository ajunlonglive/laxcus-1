/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

/**
 * 标记类型
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public final class RParameterType {

	/** 标记数据类型  **/
	public final static byte BOOLEAN = 1;
	public final static byte RAW = 2;
	public final static byte STRING = 3;

	/** 整数 **/
	public final static byte SHORT = 4;
	public final static byte INTEGER = 5;
	public final static byte LONG = 6;

	/** 浮点数 **/
	public final static byte FLOAT = 7;
	public final static byte DOUBLE = 8;

	/** 时间/日期 **/
	public final static byte DATE = 9;
	public final static byte TIME = 10;
	public final static byte TIMESTAMP = 11;
	
	/** 命令对象 **/
	public final static byte COMMAND = 12;

	/** 可类化对象 **/
	public final static byte CLASSABLE = 13;

	/** 串行化对象 **/
	public final static byte SERIALABLE = 14;

	/**
	 * 判断是标记类型
	 * @param who 标记类型
	 * @return 返回“真”或者“假”
	 */
	public static boolean isValueType(byte who) {
		switch (who) {
		case RParameterType.BOOLEAN:
		case RParameterType.RAW:
		case RParameterType.STRING:
		case RParameterType.SHORT:
		case RParameterType.INTEGER:
		case RParameterType.LONG:
		case RParameterType.FLOAT:
		case RParameterType.DOUBLE:
		case RParameterType.DATE:
		case RParameterType.TIME:
		case RParameterType.TIMESTAMP:
		case RParameterType.COMMAND:
		case RParameterType.CLASSABLE:
		case RParameterType.SERIALABLE:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是布尔类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isBoolean(byte who) {
		return who == RParameterType.BOOLEAN;
	}

	/**
	 * 判断是字节数组类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isRaw(byte who) {
		return who == RParameterType.RAW;
	}

	/**
	 * 判断是字符串类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isString(byte who) {
		return who == RParameterType.STRING;
	}

	/**
	 * 判断是短整型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isShort(byte who) {
		return who == RParameterType.SHORT;
	}

	/**
	 * 判断是整型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isInteger(byte who) {
		return who == RParameterType.INTEGER;
	}

	/**
	 * 判断是长整型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isLong(byte who) {
		return who == RParameterType.LONG;
	}

	/**
	 * 判断是单浮点值
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isFloat(byte who) {
		return who == RParameterType.FLOAT;
	}

	/**
	 * 判断是双浮点值
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isDouble(byte who) {
		return who == RParameterType.DOUBLE;
	}

	/**
	 * 判断是日期类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isDate(byte who) {
		return who == RParameterType.DATE;
	}

	/**
	 * 判断是时间类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isTime(byte who) {
		return who == RParameterType.TIME;
	}

	/**
	 * 判断是时间戳类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isTimestamp(byte who) {
		return who == RParameterType.TIMESTAMP;
	}
	
	/**
	 * 判断是命令对象
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isCommand(byte who) {
		return who == RParameterType.COMMAND;
	}

	/**
	 * 判断是可类化接口实例对象
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isClassable(byte who) {
		return who == RParameterType.CLASSABLE;
	}

	/**
	 * 判断是串行化对象类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isSerializable(byte who) {
		return who == RParameterType.SERIALABLE;
	}

}