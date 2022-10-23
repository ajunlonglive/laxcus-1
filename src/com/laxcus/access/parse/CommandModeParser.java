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
import com.laxcus.util.tip.*;

/**
 * 命令处理模式解析器 <br>
 * 
 * 在“MEMORY/DISK”两个关键字任意选择一个。以后的数据处理，将采用此次指定的模式处理。
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class CommandModeParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+COMMAND\\s+MODE)\\s+(?i)(MEMORY|DISK)\\s*$";

	/**
	 * 建立命令处理模式解析器
	 */
	public CommandModeParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET COMMAND MODE", input);
		}
		Pattern pattern = Pattern.compile(CommandModeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析语句
	 * @param input 输入语句
	 * @return 返回CommandMode命令
	 */
	public CommandMode split(String input) {
		Pattern pattern = Pattern.compile(CommandModeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String mod = matcher.group(1);
		CommandMode cmd = new CommandMode();

		boolean memory = mod.matches("^\\s*(?i)(MEMORY)\\s*$");
		cmd.setMode(memory ? CommandMode.MEMORY : CommandMode.DISK);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}