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
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 打印数据表图谱命令解析器。<br>
 * 
 * 语法格式：PRINT DATABASE DIAGRAM ALL|数据库名, ... FROM ME|用户签名, ....
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class PrintSchemaDiagramParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:PRINT\\s+DATABASE\\s+DIAGRAM)\\s+([\\w\\W]+)\\s*$";
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PRINT\\s+DATABASE\\s+DIAGRAM)\\s+(ALL|[\\w\\W]+)\\s+(?i)(?:FROM)\\s+(?i)(ME|[\\w\\W]+)\\s*$";

	/**
	 * 构造默认的打印数据表图谱命令解析器
	 */
	public PrintSchemaDiagramParser() {
		super();
	}

	/**
	 * 判断匹配“显示数据库”语句。语句前缀是：“PRINT DATABASE DIAGRAM”。
	 * @param simple 简单判断
	 * @param input　输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("PRINT DATABASE DIAGRAM", input);
		}
		Pattern pattern = Pattern.compile(PrintSchemaDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“显示数据库”配置语句。
	 * @param input 输入语句
	 * @param online 在线检查，如果是“真”，检查数据库名称存在
	 * @return 返回“PRINT DATABASE DIAGRAM”命令对象
	 */
	public PrintSchemaDiagram split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(PrintSchemaDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		PrintSchemaDiagram cmd = new PrintSchemaDiagram();
		// 输出命令
		cmd.setPrimitive(input);
		
		//1. 取数据库名
		String text = matcher.group(1);
		// 不是ALL关键字，检查全部数据库
		boolean all = text.matches("^\\s*(?i)(?:ALL)\\s*$");
		// 不是全部数据库，按照逗号，分割字符串
		if (!all) {
			String[] items = splitCommaSymbol(text);
			for (String item : items) {
				// 判断语法正确
				if (!Fame.validate(item)) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
				}
				Fame fame = new Fame(item);
				// 如果是在线模式，检查这个数据库
				if (online) {
					if (!hasSchema(fame)) {
						throwableNo(FaultTip.NOTFOUND_X, item);
					}
				}
				cmd.add(fame);
			}
		}

		// 2. 取用户签名
		text = matcher.group(2);
		boolean me = text.matches("^\\s*(?i)(?:ME)\\s*$");
		// 不是自己，按照逗号，分割字符串，逐一解析
		if (!me) {
			String[] items = splitCommaSymbol(text);
			for (String item : items) {
				// 生成一个用户签名
				Siger siger = splitSiger(item);
				cmd.addUser(siger);
				cmd.addPlainText(item);
			}
		}

		return cmd;
	}

}