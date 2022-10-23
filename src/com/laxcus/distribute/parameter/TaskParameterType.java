/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.parameter;

/**
 * 自定义参数类型
 * 
 * @author scott.liang
 * @version 1.0 5/26/2009
 * @since laxcus 1.0
 */
public final class TaskParameterType {

	/** 自定义参数数据类型  **/
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
	 * 判断是自定义参数类型
	 * @param who 自定义参数类型
	 * @return 返回“真”或者“假”
	 */
	public static boolean isValueType(byte who) {
		switch (who) {
		case TaskParameterType.BOOLEAN:
		case TaskParameterType.RAW:
		case TaskParameterType.STRING:
		case TaskParameterType.SHORT:
		case TaskParameterType.INTEGER:
		case TaskParameterType.LONG:
		case TaskParameterType.FLOAT:
		case TaskParameterType.DOUBLE:
		case TaskParameterType.DATE:
		case TaskParameterType.TIME:
		case TaskParameterType.TIMESTAMP:
		case TaskParameterType.COMMAND:
		case TaskParameterType.CLASSABLE:
		case TaskParameterType.SERIALABLE:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是布尔类型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isBoolean(byte who) {
		return who == TaskParameterType.BOOLEAN;
	}

	/**
	 * 判断是字节数组类型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isRaw(byte who) {
		return who == TaskParameterType.RAW;
	}

	/**
	 * 判断是字符串类型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isString(byte who) {
		return who == TaskParameterType.STRING;
	}

	/**
	 * 判断是短整型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isShort(byte who) {
		return who == TaskParameterType.SHORT;
	}

	/**
	 * 判断是整型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isInteger(byte who) {
		return who == TaskParameterType.INTEGER;
	}

	/**
	 * 判断是长整型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isLong(byte who) {
		return who == TaskParameterType.LONG;
	}

	/**
	 * 判断是单浮点值
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isFloat(byte who) {
		return who == TaskParameterType.FLOAT;
	}

	/**
	 * 判断是双浮点值
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isDouble(byte who) {
		return who == TaskParameterType.DOUBLE;
	}

	/**
	 * 判断是日期类型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isDate(byte who) {
		return who == TaskParameterType.DATE;
	}

	/**
	 * 判断是时间类型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isTime(byte who) {
		return who == TaskParameterType.TIME;
	}

	/**
	 * 判断是时间戳类型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isTimestamp(byte who) {
		return who == TaskParameterType.TIMESTAMP;
	}
	
	/**
	 * 判断是命令对象
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isCommand(byte who) {
		return who == TaskParameterType.COMMAND;
	}

	/**
	 * 判断是可类化接口实例对象
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isClassable(byte who) {
		return who == TaskParameterType.CLASSABLE;
	}

	/**
	 * 判断是串行化对象类型
	 * @param who 自定义参数类型
	 * @return 返回真或者假
	 */
	public static boolean isSerializable(byte who) {
		return who == TaskParameterType.SERIALABLE;
	}

}