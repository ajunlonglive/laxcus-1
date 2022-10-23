/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

/**
 * 数据存储模型，兼容JNI C接口。 <br><br>
 * 
 * NSM: n-array storage model (行存储模型)<br>
 * DSM: depress storage model (列存储模型)<br>
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public class StorageModel {

	/** 行存储模型 **/
	public static final byte NSM = 1;

	/** 列存储模型 **/
	public static final byte DSM = 2;
	
	/**
	 * 将数据存储模型翻译为字符串描述
	 * @param who 数据存储模型
	 * @return 字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case StorageModel.NSM:
			return "NSM";
		case StorageModel.DSM:
			return "DSM";
		}
		return "ILLEGAL MODEL";
	}

	/**
	 * 判断是行存储模型
	 * @param who 类型标记
	 * @return 返回真或者假
	 */
	public static boolean isNSM(byte who) {
		return who == StorageModel.NSM;
	}

	/**
	 * 判断是列存储模型
	 * @param who 类型标记
	 * @return 返回真或者假
	 */
	public static boolean isDSM(byte who) {
		return who == StorageModel.DSM;
	}

	/**
	 * 判断是行存储模型
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean isNSM(String input) {
		return input != null && input.matches("^\\s*(?i)(NSM)\\s*$");
	}

	/**
	 * 判断是列存储模型
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean isDSM(String input) {
		return input != null && input.matches("^\\s*(?i)(DSM)\\s*$");
	}
	
	/**
	 * 判断是合法的列存储模型
	 * @param who 列存储模型
	 * @return 返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case StorageModel.NSM:
		case StorageModel.DSM:
			return true;
		default:
			return false;
		}
	}

}