/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.rule.*;
import com.laxcus.util.tip.*;

/**
 * 显示分布锁事务规则解析器。<BR><BR>
 * 
 * 语法：<BR>
 * SHOW LOCK RULE <BR>
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class ShowLockRuleParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SHOW\\s+LOCK\\s+RULE)\\s*$";

	/**
	 * 构造锁定语法解析器
	 */
	public ShowLockRuleParser() {
		super();
	}

	/**
	 * 匹配语法
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(ShowLockRuleParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 生成命令
	 * @param input 输入语句
	 * @return 返回ShowRule
	 */
	public ShowLockRule split(String input) {
		Pattern pattern = Pattern.compile(ShowLockRuleParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ShowLockRule cmd = new ShowLockRule();
		cmd.setPrimitive(input);
		return cmd;
	}
}