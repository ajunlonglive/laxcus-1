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
import com.laxcus.command.access.user.*;
import com.laxcus.util.tip.*;

/**
 * 设置用户权级解析器。<br><br>
 * 
 * 语法格式：SET USER PRIORITY [权级] TO 用户名称 | SIGN 数字签名
 * 
 * @author scott.liang
 * @version 1.0 05/07/2017
 * @since laxcus 1.0
 */
public class SetUserPriorityParser extends MultiUserParameterParser {

	/** 设置设置用户权级正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+USER\\s+PRIORITY)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的设置用户权级解析器
	 */
	public SetUserPriorityParser() {
		super();
	}

	/**
	 * 判断语句匹配“SET USER PRIORITY ...”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET USER PRIORITY", input);
		}
		Pattern pattern = Pattern.compile(SetUserPriorityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析设置用户权级语句
	 * @param input 设置用户权级语句
	 * @return 返回SetUserPriority命令
	 */
	public SetUserPriority split(String input) {
		Pattern pattern = Pattern.compile(SetUserPriorityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// NONE, MIN, NORMAL, MAX，4种权级
		String value = matcher.group(1);
		int priority = CommandPriority.translate(value);
		// 判断有效
		if (priority == -1 || !(CommandPriority.NONE <= priority && priority <= CommandPriority.MAX)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, value);
		}
		
		// 以逗号为依据，分割用户签名
		String line = matcher.group(2);

		SetUserPriority cmd = new SetUserPriority(priority);

		// 解析任意多个账号签名
		splitSigers(line, cmd);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}
