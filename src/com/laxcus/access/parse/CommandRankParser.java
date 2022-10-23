/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.*;
import com.laxcus.command.mix.*;
import com.laxcus.util.tip.*;

/**
 * 命令优先级解析器 <br>
 * 只允许WATCH节点使用
 * 
 * @author scott.liang
 * @version 1.0 1/20/2020
 * @since laxcus 1.0
 */
public class CommandRankParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+COMMAND\\s+PRIORITY)\\s+(?i)(NONE|MIN|NORMAL|MAX|FAST)\\s*$";

	/**
	 * 构造命令优先级解析器
	 */
	public CommandRankParser() {
		super();
	}

	/**
	 * 判断匹配命令优先级语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET COMMAND PRIORITY", input);
		}
		Pattern pattern = Pattern.compile(CommandRankParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析命令优先级语句
	 * @param input 输入语句
	 * @return 返回CommandRank命令
	 */
	public CommandRank split(String input) {
		// 无限制时间
		Pattern pattern = Pattern.compile(CommandRankParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String who = matcher.group(1);
		// 解析
		byte priority = CommandPriority.translate(who);
		// 判断有效！
		if (!CommandPriority.isPriority(priority)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		CommandRank cmd = new CommandRank();
		// 命令优先级
		cmd.setRank(priority);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}