/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

/**
 * 列属性的索引键类型，兼容JNI C接口。<br><br>
 * 
 * 包括 ：<br>
 * 1. 主键（一个表只能有一个）<br>
 * 2. 从键（一个表有任意多个）<br>
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public class KeyType {

	/** 列属性的索引键值，见 com.laxcus.access.column.attribute **/

	/** 无定义索引键 **/
	public final static byte NONE_KEY = 0;

	/** 主键（一个表只能有一个） **/
	public final static byte PRIME_KEY = 1;

	/** 从键（一个表有任意多个） **/
	public final static byte SLAVE_KEY = 2;

	/**
	 * 判断无定义
	 * @param who 列索引键
	 * @return 返回真或者假
	 */
	public static boolean isNoneKey(byte who) {
		return who == KeyType.NONE_KEY;
	}

	/**
	 * 判断是主键
	 * @param who 列索引键
	 * @return 返回真或者假
	 */
	public static boolean isPrimeKey(byte who) {
		return who == KeyType.PRIME_KEY;
	}

	/**
	 * 判断是从键
	 * @param who 列索引键
	 * @return 返回真或者假
	 */
	public static boolean isSlaveKey(byte who) {
		return who == KeyType.SLAVE_KEY;
	}

	/**
	 * 判断是合法的列索引键
	 * @param who 列索引键
	 * @return 返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch(who) {
		case KeyType.PRIME_KEY:
		case KeyType.SLAVE_KEY:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 将数字转为字符串描述
	 * @param who 列索引键
	 * @return 返回索引键的字符串描述
	 */
	public static String translate(byte who) {
		switch(who) {
		case KeyType.PRIME_KEY:
			return "PRIME KEY";
		case KeyType.SLAVE_KEY:
			return "SLAVE KEY";
		}
		return "NONE KEY";
	}

}