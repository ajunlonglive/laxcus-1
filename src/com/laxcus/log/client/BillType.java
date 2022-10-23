/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import java.util.*;

/**
 * 操作类型 <br>
 * 包括:COST, ERROR, FATAL 三种，没有级别之分 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/13/2022
 * @since laxcus 1.0
 */
public final class BillType {
	
	/** 操作类型的字符串描述 **/
	public final static String COST_TEXT = "COST";
//	public final static String INVOKER_TEXT = "INVOKER";
//	public final static String MESSAGE_TEXT = "MESSAGE";
//	public final static String WARNING_TEXT = "WARNING";
	public final static String ERROR_TEXT = "ERROR";
	public final static String FATAL_TEXT = "FATAL";

	/** 操作类型的编号 **/
	public final static int COST = 0x1;
//	public final static int INVOKER = 0x2;
//	public final static int MESSAGE = 0x4;
//	public final static int WARNING = 0x8;
	public final static int ERROR = 0x10;
	public final static int FATAL = 0x20;
	
//	/** 全部 **/
//	public final static int ALL = COST | INVOKER | MESSAGE | WARNING | ERROR | FATAL;

	/** 全部 **/
	public final static int ALL = COST | ERROR | FATAL;

	/**
	 * 构造默认的操作类型
	 */
	public BillType() {
		super();
	}
	
	/**
	 * 判断支持消耗操作
	 * @param who 类型
	 * @return 返回真或者假
	 */
	public static boolean isCost(int who) {
		return who > 0 && (who & BillType.COST) == BillType.COST;
	}

//	/**
//	 * 判断支持调用器操作
//	 * @param who 类型
//	 * @return 返回真或者假
//	 */
//	public static boolean isInvoker(int who) {
//		return who > 0 && (who & BillType.INVOKER) == BillType.INVOKER;
//	}
//	
//	/**
//	 * 判断支持消息操作
//	 * @param who 操作类型
//	 * @return 返回真或者假
//	 */
//	public static boolean isMessage(int who) {
//		return who > 0 && (who & BillType.MESSAGE) == BillType.MESSAGE;
//	}
//
//	/**
//	 * 判断支持警告操作
//	 * @param who 操作类型
//	 * @return 返回真或者假
//	 */
//	public static boolean isWarning(int who) {
//		return who > 0 && (who & BillType.WARNING) == BillType.WARNING;
//	}

	/**
	 * 判断支持错误操作
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isError(int who) {
		return who > 0 && (who & BillType.ERROR) == BillType.ERROR;
	}

	/**
	 * 判断支持故障操作
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isFatal(int who) {
		return who > 0 && (who & BillType.FATAL) == BillType.FATAL;
	}

	/**
	 * 判断操作类型有效
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isLevel(int who) {
		switch (who) {
		case BillType.COST:
//		case BillType.INVOKER:
//		case BillType.MESSAGE:
//		case BillType.WARNING:
		case BillType.ERROR:
		case BillType.FATAL:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 返回操作类型的字符串描述
	 * @param who 操作类型
	 * @return 字符串描述
	 */
	public static String getText(int who) {
		switch(who) {
		case BillType.COST:
			return BillType.COST_TEXT;
//		case BillType.INVOKER:
//			return BillType.INVOKER_TEXT;
//		case BillType.MESSAGE:
//			return BillType.MESSAGE_TEXT;
//		case BillType.WARNING:
//			return BillType.WARNING_TEXT;
		case BillType.ERROR:
			return BillType.ERROR_TEXT;
		case BillType.FATAL:
			return BillType.FATAL_TEXT;
		}
		return "NONE";
	}

	/**
	 * 将操作类型的字符串翻译为数字描述<br>
	 * 
	 * @param input 输入语句
	 * @return 操作类型
	 */
	public static int translate(String input) {
		if (input.matches("^\\s*(?i)(COST)\\s*$")) {
			return BillType.COST;
//		} else if (input.matches("^\\s*(?i)(INVOKER)\\s*$")) {
//			return BillType.INVOKER;
//		} else if (input.matches("^\\s*(?i)(MESSAGE)\\s*$")) {
//			return BillType.MESSAGE;
//		} else if (input.matches("^\\s*(?i)(WARNING)\\s*$")) {
//			return BillType.WARNING;
		} else if (input.matches("^\\s*(?i)(ERROR)\\s*$")) {
			return BillType.ERROR;
		} else if (input.matches("^\\s*(?i)(FATAL)\\s*$")) {
			return BillType.FATAL;
		} else if (input.matches("^\\s*(?i)(ALL)\\s*$")) {
			return BillType.ALL;
		}
		return -1;
	}
	
	/**
	 * 翻译全部参数，转换成指定值
	 * @param input 输入参数
	 * @return 转换后的数字
	 */
	public static int translateAll(String input) {
		String[] subs = input.split("\\s*\\,\\s*");
		if (subs == null) {
			return 0;
		}
		// 逐一合并
		int value = 0;
		for (String sub : subs) {
			int type = BillType.translate(sub);
			if (type > 0) {
				value |= type;
			}
		}
		return value;
	}
	
	/**
	 * 根据类型，转换成多个文本描述
	 * @param who 数字
	 * @return 字符串数组，没有是空指针
	 */
	public static String[] translateAll(int who) {
		ArrayList<String> a = new ArrayList<String>();
		// 解析参数
		if (BillType.isCost(who)) {
			a.add(BillType.COST_TEXT);
		}
//		if (BillType.isInvoker(who)) {
//			a.add(BillType.INVOKER_TEXT);
//		}
//		if (BillType.isMessage(who)) {
//			a.add(BillType.MESSAGE_TEXT);
//		}
//		if (BillType.isWarning(who)) {
//			a.add(BillType.WARNING_TEXT);
//		}
		if (BillType.isError(who)) {
			a.add(BillType.ERROR_TEXT);
		}
		if (BillType.isFatal(who)) {
			a.add(BillType.FATAL_TEXT);
		}

		if (a.isEmpty()) {
			return null;
		}
		String[] strs = new String[a.size()];
		return a.toArray(strs);
	}
	
	/**
	 * 转换成字符串输出
	 * @param who 类型
	 * @return 字符串
	 */
	public static String translateString(int who) {
		StringBuilder bf = new StringBuilder();
		String[] subs = BillType.translateAll(who);
		if (subs == null) {
			return "";
		}
		for (int i = 0; i < subs.length; i++) {
			if (i > 0) {
				bf.append(",");
			}
			bf.append(subs[i]);
		}
		return bf.toString();
	}
	
//	public static void main(String[] args) {
//		int type = BillType.ALL;
//		type = (type | BillType.MESSAGE);
//		
//		String str = BillType.translateString(type); //CostType.ALL);
//		System.out.println(str);
//		
//		SHA256Hash hash = new SHA256Hash((byte)0);
//		System.out.printf("hash code is %d\n" , hash.hashCode());
//		hash = new SHA256Hash((byte)1);
//		System.out.printf("hash code is %d\n" , hash.hashCode());
//	}
}