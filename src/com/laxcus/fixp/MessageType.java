/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

/**
 * 消息类型
 * 
 * @author scott.liang
 * @version 1.0 11/23/2009
 * @since laxcus 1.0
 */
public final class MessageType {

	/** FIXP消息的数值类型 (4位) */
	public static final byte RAW = 1;
	public static final byte BOOLEAN = 2;
	public static final byte STRING = 3;
	public static final byte INT16 = 4;
	public static final byte INT32 = 5;
	public static final byte INT64 = 6;
	public static final byte REAL32 = 7;
	public static final byte REAL64 = 8;
	
	/**
	 * 判断是规定范围的数据类型
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isType(byte who) {
		switch (who) {
		case MessageType.RAW:
		case MessageType.BOOLEAN:
		case MessageType.STRING:
		case MessageType.INT16:
		case MessageType.INT32:
		case MessageType.INT64:
		case MessageType.REAL32:
		case MessageType.REAL64:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是二进制格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isRaw(byte who) {
		return who == MessageType.RAW;
	}

	/**
	 * 判断是字符串格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isString(byte who) {
		return who == MessageType.STRING;
	}

	/**
	 * 判断是布尔格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isBoolean(byte who) {
		return who == MessageType.BOOLEAN;
	}

	/**
	 * 判断是短整型格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isShort(byte who) {
		return who == MessageType.INT16;
	}

	/**
	 * 判断是整型格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isInteger(byte who) {
		return who == MessageType.INT32;
	}

	/**
	 * 判断是长整型格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isLong(byte who) {
		return who == MessageType.INT64;
	}

	/**
	 * 判断是单浮点格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isFloat(byte who) {
		return who == MessageType.REAL32;
	}

	/**
	 * 判断是双浮点格式
	 * @param who 数据类型
	 * @return 返回真或者假
	 */
	public static boolean isDouble(byte who) {
		return who == MessageType.REAL64;
	}

}