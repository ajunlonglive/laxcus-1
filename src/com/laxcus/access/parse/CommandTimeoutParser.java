/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 命令超时解析器 <br>
 * 
 * 在“小时、分钟、秒”三个单位之间选择，或者不限制时间。
 * FRONT站点的命令超时时间，将以此次的设置是单位执行。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class CommandTimeoutParser extends SyntaxParser {

	/** 无限制时间 **/
	private final static String UNLIMIT = "^\\s*(?i)(?:SET\\s+COMMAND\\s+TIMEOUT)\\s+(?i)(UNLIMIT)\\s*$";

	/** 限制时间 **/
	private final static String LIMIT = "^\\s*(?i)(?:SET\\s+COMMAND\\s+TIMEOUT)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造命令超时解析器
	 */
	public CommandTimeoutParser() {
		super();
	}

	/**
	 * 判断匹配命令超时语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET COMMAND TIMEOUT", input);
		}
		Pattern pattern = Pattern.compile(CommandTimeoutParser.UNLIMIT);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(CommandTimeoutParser.LIMIT);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 解析命令超时语句
	 * @param input 输入语句
	 * @return 返回CommandTimeout命令
	 */
	public CommandTimeout split(String input) {
		CommandTimeout cmd = new CommandTimeout();
		// 保存命令原语
		cmd.setPrimitive(input);

		// 无限制时间 
		Pattern pattern = Pattern.compile(CommandTimeoutParser.UNLIMIT);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			cmd.setInterval(-1L);
			return cmd;
		}

		pattern = Pattern.compile(CommandTimeoutParser.LIMIT);
		matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 解析时间
		long interval = ConfigParser.splitTime(matcher.group(1), -1);
		if (interval < 1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		cmd.setInterval(interval);

		return cmd;
	}

}