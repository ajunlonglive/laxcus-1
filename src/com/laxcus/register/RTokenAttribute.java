/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

/**
 * 标记属性
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public final class RTokenAttribute {

	/** 参数属性  **/
	public final static byte PARAMETER = 1;

	/** 文件夹属性 **/
	public final static byte FOLDER = 2;

	/**
	 * 判断是标记属性
	 * @param who 标记属性
	 * @return 返回“真”或者“假”
	 */
	public static boolean isAttribute(byte who) {
		switch (who) {
		case RTokenAttribute.PARAMETER:
		case RTokenAttribute.FOLDER:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 转成字符串描述
	 * @param who
	 * @return
	 */
	public static String translate(byte who) {
		switch (who) {
		case RTokenAttribute.PARAMETER:
			return "PARAMETER";
		case RTokenAttribute.FOLDER:
			return "FOLDER";
		default:
			return "Unknown";
		}
	}

	/**
	 * 判断是参数
	 * @param who 标记属性
	 * @return 返回真或者假
	 */
	public static boolean isParameter(byte who) {
		return who == RTokenAttribute.PARAMETER;
	}

	/**
	 * 判断是文件夹
	 * @param who 标记属性
	 * @return 返回真或者假
	 */
	public static boolean isFolder(byte who) {
		return who == RTokenAttribute.FOLDER;
	}

}