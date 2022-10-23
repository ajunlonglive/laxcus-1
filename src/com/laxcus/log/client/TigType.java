/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import java.util.*;

import com.laxcus.util.hash.*;

/**
 * 操作类型 <br>
 * 包括:COMMAND, MESSAGE, WARNING, ERROR, FATAL五种，没有级别之分 <br>
 * 
 * @author scott.liang
 * @version 1.0 1/6/2020
 * @since laxcus 1.0
 */
public final class TigType {
	
	/** 操作类型的字符串描述 **/
	public final static String COMMAND_TEXT = "COMMAND";
	public final static String INVOKER_TEXT = "INVOKER";
	public final static String MESSAGE_TEXT = "MESSAGE";
	public final static String WARNING_TEXT = "WARNING";
	public final static String ERROR_TEXT = "ERROR";
	public final static String FATAL_TEXT = "FATAL";

	/** 操作类型的编号 **/
	public final static int COMMAND = 0x1;
	public final static int INVOKER = 0x2;
	public final static int MESSAGE = 0x4;
	public final static int WARNING = 0x8;
	public final static int ERROR = 0x10;
	public final static int FATAL = 0x20;
	
	/** 全部 **/
	public final static int ALL = COMMAND | INVOKER | MESSAGE | WARNING | ERROR | FATAL;

	/**
	 * 构造默认的操作类型
	 */
	public TigType() {
		super();
	}
	
	/**
	 * 判断支持命令操作
	 * @param who 类型
	 * @return 返回真或者假
	 */
	public static boolean isCommand(int who) {
		return who > 0 && (who & TigType.COMMAND) == TigType.COMMAND;
	}

	/**
	 * 判断支持调用器操作
	 * @param who 类型
	 * @return 返回真或者假
	 */
	public static boolean isInvoker(int who) {
		return who > 0 && (who & TigType.INVOKER) == TigType.INVOKER;
	}
	
	/**
	 * 判断支持消息操作
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isMessage(int who) {
		return who > 0 && (who & TigType.MESSAGE) == TigType.MESSAGE;
	}

	/**
	 * 判断支持警告操作
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isWarning(int who) {
		return who > 0 && (who & TigType.WARNING) == TigType.WARNING;
	}

	/**
	 * 判断支持错误操作
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isError(int who) {
		return who > 0 && (who & TigType.ERROR) == TigType.ERROR;
	}

	/**
	 * 判断支持故障操作
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isFatal(int who) {
		return who > 0 && (who & TigType.FATAL) == TigType.FATAL;
	}

	/**
	 * 判断操作类型有效
	 * @param who 操作类型
	 * @return 返回真或者假
	 */
	public static boolean isLevel(int who) {
		switch (who) {
		case TigType.COMMAND:
		case TigType.INVOKER:
		case TigType.MESSAGE:
		case TigType.WARNING:
		case TigType.ERROR:
		case TigType.FATAL:
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
		case TigType.COMMAND:
			return TigType.COMMAND_TEXT;
		case TigType.INVOKER:
			return TigType.INVOKER_TEXT;
		case TigType.MESSAGE:
			return TigType.MESSAGE_TEXT;
		case TigType.WARNING:
			return TigType.WARNING_TEXT;
		case TigType.ERROR:
			return TigType.ERROR_TEXT;
		case TigType.FATAL:
			return TigType.FATAL_TEXT;
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
		if (input.matches("^\\s*(?i)(COMMAND)\\s*$")) {
			return TigType.COMMAND;
		} else if (input.matches("^\\s*(?i)(INVOKER)\\s*$")) {
			return TigType.INVOKER;
		} else if (input.matches("^\\s*(?i)(MESSAGE)\\s*$")) {
			return TigType.MESSAGE;
		} else if (input.matches("^\\s*(?i)(WARNING)\\s*$")) {
			return TigType.WARNING;
		} else if (input.matches("^\\s*(?i)(ERROR)\\s*$")) {
			return TigType.ERROR;
		} else if (input.matches("^\\s*(?i)(FATAL)\\s*$")) {
			return TigType.FATAL;
		} else if (input.matches("^\\s*(?i)(ALL)\\s*$")) {
			return TigType.ALL;
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
			int type = TigType.translate(sub);
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
		if (TigType.isCommand(who)) {
			a.add(TigType.COMMAND_TEXT);
		}
		if (TigType.isInvoker(who)) {
			a.add(TigType.INVOKER_TEXT);
		}
		if (TigType.isMessage(who)) {
			a.add(TigType.MESSAGE_TEXT);
		}
		if (TigType.isWarning(who)) {
			a.add(TigType.WARNING_TEXT);
		}
		if (TigType.isError(who)) {
			a.add(TigType.ERROR_TEXT);
		}
		if (TigType.isFatal(who)) {
			a.add(TigType.FATAL_TEXT);
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
		String[] subs = TigType.translateAll(who);
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
	
	public static void main(String[] args) {
		int type = TigType.ALL;
		type = (type | TigType.MESSAGE);
		
		String str = TigType.translateString(type); //TigType.ALL);
		System.out.println(str);
		
		SHA256Hash hash = new SHA256Hash((byte)0);
		System.out.printf("hash code is %d\n" , hash.hashCode());
		hash = new SHA256Hash((byte)1);
		System.out.printf("hash code is %d\n" , hash.hashCode());
	}
}