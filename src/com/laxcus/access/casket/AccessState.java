/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

/**
 * JNI数据存取的结果状态
 * 
 * @author scott.liang
 * @version 1.0 11/02/2012
 * @since laxcus 1.0
 */
public final class AccessState {

	/** 处理结果状态 **/
	
	/** JNI故障 **/
	public final static byte FAULT = -1;

	/** 没有数据块 **/
	public final static byte NOT_FOUND = 0;

	/** 操作结果数据在内存 **/
	public final static byte MEMORY = 1;

	/** 操作结果数据在硬盘 **/
	public final static byte DISK = 2;

	/**
	 * 判断是合法的状态符
	 * @param who 状态符
	 * @return 返回真或者假
	 */
	public static boolean isState(byte who) {
		switch (who) {
		case AccessState.MEMORY:
		case AccessState.DISK:
		case AccessState.NOT_FOUND:
		case AccessState.FAULT:
			return true;
		}
		return false;
	}
	
	/**
	 * 转成字符串描述
	 * @param who 状态符
	 * @return 字符串说明
	 */
	public static String translate(byte who) {
		switch (who) {
		case AccessState.MEMORY:
			return "Memory";
		case AccessState.DISK:
			return "Disk";
		case AccessState.NOT_FOUND:
			return "Not Found";
		case AccessState.FAULT:
			return "Fault";
		}
		return "I DO NOT KNOWN";
	}

	/**
	 * 判断数据在内存
	 * @param who 状态符
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isMemory(byte who) {
		return who == AccessState.MEMORY;
	}

	/**
	 * 判断数据在磁盘
	 * @param who 状态符
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isDisk(byte who) {
		return who == AccessState.DISK;
	}

	/**
	 * 判断没有找到
	 * @param who 状态符
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isNotFound(byte who) {
		return who == AccessState.NOT_FOUND;
	}

	/**
	 * 判断执行过程中发生错误
	 * @param who 状态符
	 * @return 条件成立返回“真”，否则“假”
	 */
	public static boolean isFault(byte who) {
		return who == AccessState.FAULT;
	}
}