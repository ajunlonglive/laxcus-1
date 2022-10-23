/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.access.user.*;
import com.laxcus.util.tip.*;

/**
 * 扫描用户日志解析器
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 **/
public class ScanUserLogParser extends MultiUserParser {

	/** 正则表达式 **/
	private final static String REGEX1 = "^\\s*(?i)(?:SCAN\\s+USER\\s+LOG)\\s+([\\w\\W]+)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	private final static String REGEX2 = "^\\s*(?i)(?:SCAN\\s+USER\\s+LOG)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的扫描用户日志解析器
	 */
	public ScanUserLogParser() {
		super();
	}

	/**
	 * 检查输入语句匹配本命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SCAN USER LOG", input);
		}
		Pattern pattern = Pattern.compile(ScanUserLogParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(ScanUserLogParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析“SCAN USER LOG”语句
	 * @param input 输入语句
	 * @return 返回ScanUserLog命令
	 */
	public ScanUserLog split(String input) {
		ScanUserLog cmd = new ScanUserLog();

		String line = null;
		String from = null;
		String to = null;

		// 第一种格式
		Pattern pattern = Pattern.compile(ScanUserLogParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			line = matcher.group(1);
			from = matcher.group(2);
			to = matcher.group(3);
		}
		// 第二种格式
		if (!match) {
			pattern = Pattern.compile(ScanUserLogParser.REGEX2);
			matcher = pattern.matcher(input);
			if (match = matcher.matches()) {
				line = matcher.group(1);
			}
		}
		// 以上不正确是错误
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX, input);
		}

		// 以逗号为依据，分割用户签名
		String[] users = splitCommaSymbol(line);
		// 解析用户签名单元
		for (String username : users) {
			splitSigers(username, cmd);
		}

		// 解析时间范围
		if (from != null && to != null) {

		}

		// 保存原语
		cmd.setPrimitive(input);

		return cmd;
	}

}