/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

/**
 * 日志级别 <br>
 * 包括:DEBUG, INFO, WARNING, ERROR, FATAL，共五层。 <br>
 * 
 * @author scott.liang
 * @version 1.0 5/3/2009
 * @since laxcus 1.0
 */
public final class LogLevel {

	/** 日志级别的字符串描述 **/
	public final static String DebugText = "DEBUG";
	public final static String InfoText = "INFO";
	public final static String WarningText = "WARNING";
	public final static String ErrorText = "ERROR";
	public final static String FatalText = "FATAL";

	/** 日志级别的编号 **/
	public final static int DEBUG = 1;
	public final static int INFO = 2;
	public final static int WARNING = 3;
	public final static int ERROR = 4;
	public final static int FATAL = 5;

	/** 不发送，超过以上全部级别 **/
	public final static int NONE = 100;

	/**
	 * 构造默认的日志级别
	 */
	public LogLevel() {
		super();
	}

	/**
	 * 判断日志级别有效
	 * @param who 日志级别
	 * @return 返回真或者假
	 */
	public static boolean isLevel(int who) {
		switch (who) {
		case LogLevel.DEBUG:
		case LogLevel.INFO:
		case LogLevel.WARNING:
		case LogLevel.ERROR:
		case LogLevel.FATAL:
		case LogLevel.NONE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 返回日志级别的字符串描述
	 * @param who 日志级别
	 * @return 字符串描述
	 */
	public static String getText(int who) {
		switch(who) {
		case LogLevel.DEBUG:
			return LogLevel.DebugText;
		case LogLevel.INFO:
			return LogLevel.InfoText;
		case LogLevel.WARNING:
			return LogLevel.WarningText;
		case LogLevel.ERROR:
			return LogLevel.ErrorText;
		case LogLevel.FATAL:
			return LogLevel.FatalText;
		}
		return "NONE";
	}

	/**
	 * 将日志级别的字符串翻译为数字描述<br>
	 * 
	 * @param input - 输入语句
	 * @return - 日志级别
	 */
	public static int translate(String input) {
		if (input.matches("^\\s*(?i)(DEBUG)\\s*$")) {
			return LogLevel.DEBUG;
		} else if (input.matches("^\\s*(?i)(INF|INFO|INFOR|INFORMATION)\\s*$")) {
			return LogLevel.INFO;
		} else if (input.matches("^\\s*(?i)(WARN|WARNING)\\s*$")) {
			return LogLevel.WARNING;
		} else if (input.matches("^\\s*(?i)(ERR|ERROR)\\s*$")) {
			return LogLevel.ERROR;
		} else if (input.matches("^\\s*(?i)(FATAL)\\s*$")) {
			return LogLevel.FATAL;
		}
		return -1;
	}
}