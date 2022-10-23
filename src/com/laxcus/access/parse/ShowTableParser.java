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
 * 显示表命令解析器。<br><br>
 * 
 * 语法格式1：SHOW TABLE 数据库.表, 数据库.表, ... <br>
 * 语法格式2：SHOW TABLE ALL <br>
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class ShowTableParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SHOW\\s+TABLE)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的显示表命令解析器
	 */
	public ShowTableParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SHOW TABLE", input);
		}
		Pattern pattern = Pattern.compile(ShowTableParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回ShowTable命令
	 */
	public ShowTable split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ShowTableParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		ShowTable cmd = new ShowTable();
		cmd.setPrimitive(input); // 保存原语

		String text = matcher.group(1);
		// 不是ALL关键字，检查全部数据库
		boolean match = text.matches("^\\s*(?i)(?:ALL)\\s*$");
		if (match) {
			return cmd;
		}

		// 按照逗号，分割字符串，逐一解析
		String[] items = splitCommaSymbol(text);
		for (String item : items) {
			// 判断表名正确
			if(!Space.validate(item)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			Space space = new Space(item);
			// 如果是在线模式，检查这个数据表
			if (online) {
				if (!hasTable(space)) {
					throwableNo(FaultTip.NOTFOUND_X, space);
				}
			}
			cmd.add(space);
		}

		return cmd;
	}	

}
