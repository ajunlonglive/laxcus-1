/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

/**
 * 重构时间触发标识
 * 
 * @author scott.liang
 * @version 1.0 10/13/2012
 * @since laxcus 1.0
 */
public final class SwitchTimeTag {

	/** 触发时间定义，包括：每小时、每天、每周、每月 **/
	
	/** 按小时触发 **/
	public final static byte HOURLY = 1;

	/** 按天触发 **/
	public final static byte DAILY = 2;

	/** 按星期触发 **/
	public final static byte WEEKLY = 3;

	/** 按月触发 **/
	public final static byte MONTHLY = 4;

	/**
	 * 判断是合法的类型
	 * @param who 触发类型
	 * @return 返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case SwitchTimeTag.HOURLY:
		case SwitchTimeTag.DAILY:
		case SwitchTimeTag.WEEKLY:
		case SwitchTimeTag.MONTHLY:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 翻译参数
	 * @param who 触发类型
	 * @return 返回对应的文本
	 */
	public static String translate(byte who) {
		switch (who) {
		case SwitchTimeTag.HOURLY:
			return "Hourly";
		case SwitchTimeTag.DAILY:
			return "Daily";
		case SwitchTimeTag.WEEKLY:
			return "Weekly";
		case SwitchTimeTag.MONTHLY:
			return "Monthly";
		default:
			return "None";
		}
	}
	
	/**
	 * 判断按照小时触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isHourly(byte who) {
		return who == SwitchTimeTag.HOURLY;
	}

	/**
	 * 判断按天触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isDaily(byte who) {
		return who == SwitchTimeTag.DAILY;
	}

	/**
	 * 判断按星期触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isWeekly(byte who) {
		return who == SwitchTimeTag.WEEKLY;
	}

	/**
	 * 判断按月触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isMonthly(byte who) {
		return who == SwitchTimeTag.MONTHLY;
	}
	
	/**
	 * 判断按照小时触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isHourly(String who) {
		return who.matches("^\\s*(?i)(HOURLY)\\s*$");
	}

	/**
	 * 判断按天触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isDaily(String who) {
		return who.matches("^\\s*(?i)(DAILY)\\s*$");
	}

	/**
	 * 判断按星期触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isWeekly(String who) {
		return who.matches("^\\s*(?i)(WEEKLY)\\s*$");
	}

	/**
	 * 判断按月触发
	 * @param who 触发标识
	 * @return 匹配返回真，否则假
	 */
	public static boolean isMonthly(String who) {
		return who.matches("^\\s*(?i)(MONTHLY)\\s*$");
	}
	
	
}