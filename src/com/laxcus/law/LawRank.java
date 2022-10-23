/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law;

/**
 * 策略执行级别 <br><br>
 * 
 * 策略执行级别是在处理数据之前，确定一个操作范围。<br>
 * 策略执行级别共有3个级别，上级覆盖下级的全部资源，从高到低依次是：<br>
 * 1. 用户级策略。<br>
 * 2. 数据库级策略。<br>
 * 3. 数据表级策略。<br>
 * 4. 行级策略。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 4/13/2018
 * @since laxcus 1.0
 */
public final class LawRank {

	/** 用户级策略 **/
	public final static byte USER = 1;

	/** 数据库级策略 **/
	public final static byte SCHEMA = 2;

	/** 表级策略 **/
	public final static byte TABLE = 3;
	
	/** 列级策略 **/
	public final static byte ROW = 4;

	/**
	 * 判断是合法的策略级别
	 * @param who 策略级别
	 * @return 返回真或者假
	 */
	public static boolean isRank(byte who) {
		switch (who) {
		case LawRank.USER:
		case LawRank.SCHEMA:
		case LawRank.TABLE:
		case LawRank.ROW:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是用户级
	 * @param who 策略执行级别
	 * @return 返回真或者假
	 */
	public static boolean isUser(byte who) {
		return who == LawRank.USER;
	}

	/**
	 * 判断是数据库级
	 * @param who 策略执行级别
	 * @return 返回真或者假
	 */
	public static boolean isSchema(byte who) {
		return who == LawRank.SCHEMA;
	}

	/**
	 * 判断是表级
	 * @param who 策略执行级别
	 * @return 返回真或者假
	 */
	public static boolean isTable(byte who) {
		return who == LawRank.TABLE;
	}
	
	/**
	 * 判断是行级
	 * @param who 策略执行级别
	 * @return 返回真或者假
	 */
	public static boolean isRow(byte who) {
		return who == LawRank.ROW;
	}

	/**
	 * 返回策略级别描述
	 * @param who 策略执行级别
	 * @return 返回字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case LawRank.ROW:
			return "ROW POLICY";
		case LawRank.TABLE:
			return "TABLE POLICY";
		case LawRank.SCHEMA:
			return "SCHEMA POLICY";
		case LawRank.USER:
			return "USER POLICY";
		}
		return "ILLEGAL POLICY RANK";
	}

}