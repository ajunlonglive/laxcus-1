/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

/**
 * 表的一台计算机上的存在模式，共享或者独占。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2009
 * @since laxcus 1.0
 */
public final class TableMode {

	/** 共享模式 **/
	public static final int SHARE = 1;

	/** 独占模式 **/
	public static final int EXCLUSIVE = 2;

	/**
	 * 判断是有效的存在模式
	 * @param who  存在模式
	 * @return 返回真或者假
	 */
	public static boolean isFamily(int who) {
		// 判断类型
		switch (who) {
		case TableMode.SHARE:
		case TableMode.EXCLUSIVE:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是共享模式
	 * @return 返回真或者假
	 */
	public static boolean isShare(int who) {
		return who == TableMode.SHARE;
	}

	/**
	 * 判断是独占模式
	 * @return 返回真或者假
	 */
	public static boolean isExclusive(int who) {
		return who == TableMode.EXCLUSIVE;
	}	

	/**
	 * 将存在模式翻译为字符串描述
	 * @param who 存在模式
	 * @return 字符串
	 */
	public static String translate(int who) {
		switch (who) {
		case TableMode.SHARE:
			return "Share";
		case TableMode.EXCLUSIVE:
			return "Exclusive";
		default:
			return "None";
		}
	}

	/**
	 * 将字符串翻译为数字模式
	 * @param input 存在模式文本描述
	 * @return 存在模式的数字描述
	 */
	public static int translate(String input) {
		if (input.matches("^\\s*(?i)(?:SHARE)\\s*$")) {
			return TableMode.SHARE;
		} else if (input.matches("^\\s*(?i)(?:EXCLUSIVE)\\s*$")) {
			return TableMode.EXCLUSIVE;
		}
		return -1;
	}

}
