/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.limit.*;
import com.laxcus.util.tip.*;

/**
 * 显示限制操作单元解析器
 * 
 * 语法：SHOW LIMIT
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class ShowLimitParser extends SyntaxParser {

	private final static String REGEX = "^\\s*(?i)(?:SHOW\\s+LIMIT)\\s*$";

	/**
	 * 构造锁定语法解析器
	 */
	public ShowLimitParser() {
		super();
	}

	/**
	 * 匹配语法
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(ShowLimitParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 生成命令
	 * @param input 输入语句
	 * @return 返回ShowLimit
	 */
	public ShowLimit split(String input) {
		Pattern pattern = Pattern.compile(ShowLimitParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ShowLimit cmd = new ShowLimit();
		cmd.setPrimitive(input);
		return cmd;
	}
}