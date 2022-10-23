/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.util.tip.*;

/**
 * 判断数据库解析器 <br><br>
 * 
 * 语法格式：<br>
 * ASSERT DATABASE 数据库名  <br>
 * 
 * @author scott.liang
 * @version 1.0 12/13/2019
 * @since laxcus 1.0
 */
public class AssertSchemaParser extends SyntaxParser {

	/** 判断数据库  */
	private final static String ASSERT_DATABASE = "^\\s*(?i)(?:ASSERT\\s+DATABASE)\\s+([^\\s]{1,})\\s*$";

	/**
	 * 构造默认的判断数据库解析器
	 */
	public AssertSchemaParser() {
		super();
	}

	/**
	 * 检查语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("ASSERT DATABASE", input);
		}
		Pattern pattern = Pattern.compile(AssertSchemaParser.ASSERT_DATABASE);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 判断数据库<br>
	 * @param input 格式: ASSERT DATABASE 数据库名 
	 * @return 返回AssertSchema实例
	 */
	public AssertSchema split(String input) {
		Pattern pattern = Pattern.compile(AssertSchemaParser.ASSERT_DATABASE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String name = matcher.group(1);

		// "ALL"是关键字，不允许使用"ALL"建立数据库名称
		if (name.matches("^\\s*(?i)(?:ALL)\\s*$")) {
			throwableNo(FaultTip.FORBID_KEYWORD_X, name);
		}
		// 判断有效
		if (!Fame.validate(name)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, name);
		}

		Fame fame = new Fame(name);

		// 检测数据库
		AssertSchema cmd = new AssertSchema(fame);
		cmd.setPrimitive(input);
		return cmd;
	}

}