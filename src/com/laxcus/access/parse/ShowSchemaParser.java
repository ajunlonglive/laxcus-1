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
 * 显示数据库配置命令解析器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class ShowSchemaParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SHOW\\s+DATABASE)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的显示数据库配置命令解析器
	 */
	public ShowSchemaParser() {
		super();
	}

	/**
	 * 判断匹配“显示数据库”语句。语句前缀是：“SHOW DATABASE”。
	 * @param input　输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SHOW DATABASE", input);
		}
		Pattern pattern = Pattern.compile(ShowSchemaParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“显示数据库”配置语句。“SHOW DATABASE 数据库名, ... | ALL“
	 * @param input 输入语句
	 * @param online 在线检查，如果是“真”，检查数据库名称存在
	 * @return 返回“SHOW DATABASE”命令对象
	 */
	public ShowSchema split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ShowSchemaParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ShowSchema cmd = new ShowSchema();
		// 输出命令
		cmd.setPrimitive(input);
		
		String text = matcher.group(1);
		// 不是ALL关键字，检查全部数据库
		boolean match = text.matches("^\\s*(?i)(?:ALL)\\s*$");
		if (match) {
			return cmd;
		}

		// 按照逗号，分割字符串
		String[] items = splitCommaSymbol(text);
		for (String item : items) {
			Fame fame = new Fame(item);
			// 如果是在线模式，检查这个数据库
			if (online) {
				if (!hasSchema(fame)) {
					throwableNo(FaultTip.NOTFOUND_X, fame);
				}
			}
			cmd.add(fame);
		}

		
		return cmd;
	}

}
