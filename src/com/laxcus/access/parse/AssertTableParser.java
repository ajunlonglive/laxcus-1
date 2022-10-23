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
import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;

/**
 * 判断数据表解析器 <br><br>
 * 
 * 语法格式：<br>
 * ASSERT TABLE 数据表名  <br>
 * 
 * @author scott.liang
 * @version 1.0 4/6/2021
 * @since laxcus 1.0
 */
public class AssertTableParser extends SyntaxParser {

	/** 判断数据表  */
	private final static String ASSERT_TABLE = "^\\s*(?i)(?:ASSERT\\s+TABLE)\\s+([^\\s]{1,})\\s*$";

	/**
	 * 构造默认的判断数据表解析器
	 */
	public AssertTableParser() {
		super();
	}

	/**
	 * 检查语句匹配。
	 * @param input 输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("ASSERT TABLE", input);
		}
		Pattern pattern = Pattern.compile(AssertTableParser.ASSERT_TABLE);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 判断数据表<br>
	 * @param input 格式: ASSERT TABLE 数据表名 
	 * @return 返回AssertTable实例
	 */
	public AssertTable split(String input) {
		Pattern pattern = Pattern.compile(AssertTableParser.ASSERT_TABLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String name = matcher.group(1);
		// 判断有效
		if (!Space.validate(name)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, name);
		}

		Space space = new Space(name);

		// 检测数据表
		AssertTable cmd = new AssertTable(space);
		cmd.setPrimitive(input);
		return cmd;
	}

}