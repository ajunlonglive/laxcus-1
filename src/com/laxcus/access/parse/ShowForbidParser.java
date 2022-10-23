/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.forbid.*;
import com.laxcus.util.tip.*;

/**
 * 显示禁止操作单元解析器
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */
public class ShowForbidParser extends SyntaxParser {

	private final static String REGEX = "^\\s*(?i)(?:SHOW\\s+FORBID)\\s*$";

	/**
	 * 构造显示禁止操作单元解析器
	 */
	public ShowForbidParser() {
		super();
	}

	/**
	 * 匹配语法
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(ShowForbidParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 生成命令
	 * @param input 输入语句
	 * @return 返回ShowForbid
	 */
	public ShowForbid split(String input) {
		Pattern pattern = Pattern.compile(ShowForbidParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ShowForbid cmd = new ShowForbid();
		cmd.setPrimitive(input);
		return cmd;
	}
}