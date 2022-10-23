/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.util.tip.*;

/**
 * 打印数据优化时间解析器
 * 
 * 语法格式：PRINT REGULATE TIME 数据库名.表名, ....
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class PrintRegulateTimeParser extends SyntaxParser {

	/** 正则表达式 **/
	private static final String REGEX = "^\\s*(?i)(?:PRINT\\s+REGULATE\\s+TIME)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的打印数据优化时间解析器
	 */
	public PrintRegulateTimeParser() {
		super();
	}

	/**
	 * 判断匹配“PRINT REGULATE TIME”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("PRINT REGULATE TIME", input);
		}
		Pattern pattern = Pattern.compile(PrintRegulateTimeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“PRINT REGULATE TIME”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回PrintRegulateTime命令
	 */
	public PrintRegulateTime split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(PrintRegulateTimeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 数据表名
		String suffix = matcher.group(1);

		// 命令
		PrintRegulateTime cmd = new PrintRegulateTime();
		// 不要求显示全部，解析数据表名
		if (!isAllKeyword(suffix)) {
			List<Space> spaces = splitSpaces(suffix, online);
			cmd.addAll(spaces);
		}

		// 命令原语
		cmd.setPrimitive(input);

		// 返回结果
		return cmd;
	}
}
