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
 * 最大HOME子域集群数解析器。<br><br>
 * 
 * 语法格式：SET MAX GROUPS 数字 TO 用户名称 | SIGN 数字签名
 * 
 * @author scott.liang
 * @version 1.0 03/24/2018
 * @since laxcus 1.0
 */
public class SetMaxGroupsParser extends MultiUserParameterParser {

	/** 设置最大HOME子域集群数正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+MAX\\s+GROUPS)\\s+([1-9][0-9]{0,4})\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的最大HOME子域集群数解析器
	 */
	public SetMaxGroupsParser() {
		super();
	}

	/**
	 * 判断语句匹配“SET MAX GROUPS ...”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET MAX GROUPS", input);
		}
		Pattern pattern = Pattern.compile(SetMaxGroupsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析最大HOME子域集群数语句
	 * @param input 最大HOME子域集群数语句
	 * @return 返回SetMaxGroups命令
	 */
	public SetMaxGroups split(String input) {
		Pattern pattern = Pattern.compile(SetMaxGroupsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String number = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String line = matcher.group(2);

		SetMaxGroups cmd = new SetMaxGroups(Integer.parseInt(number));

		// 解析任意多个账号签名
		splitSigers(line, cmd);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}