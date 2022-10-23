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
 * 图形界面日志可显示数目解析器 <br>
 * 
 * 语法：SET LOG ELEMENTS 数目
 * 
 * @author scott.liang
 * @version 1.0 11/7/2019
 * @since laxcus 1.0
 */
public class MaxLogElementsParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+LOG\\s+ELEMENTS)\\s+([0-9][0-9]*)\\s*$";

	/**
	 * 构造图形界面日志可显示数目解析器
	 */
	public MaxLogElementsParser() {
		super();
	}

	/**
	 * 判断匹配语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET LOG ELEMENTS", input);
		}
		Pattern pattern = Pattern.compile(MaxLogElementsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析图形界面日志可显示数目语句
	 * @param input 输入语句
	 * @return 返回MaxLogElements命令
	 */
	public MaxLogElements split(String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(MaxLogElementsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String prefix = matcher.group(1);

		// 判断内存参数
		if (!ConfigParser.isInteger(prefix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
		}

		// 解析日志单元数
		int capacity = ConfigParser.splitInteger(prefix, 0);
		// 定义命令
		MaxLogElements cmd = new MaxLogElements(capacity);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}