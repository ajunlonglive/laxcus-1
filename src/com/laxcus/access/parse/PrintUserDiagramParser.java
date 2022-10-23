/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 显示注册用户状态命令解析器。<br><br>
 * 
 * 语法格式1：PRINT USER DIAGRAM 用户名, SIGN 用户签名, ... <br>
 * 语法格式2：PRINT USER DIAGRAM ME <br>
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class PrintUserDiagramParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PRINT\\s+USER\\s+DIAGRAM\\s+FROM)\\s+(?i)(ME|[\\w\\W]+)\\s*$";

	/**
	 * 构造默认的显示注册用户状态命令解析器
	 */
	public PrintUserDiagramParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("PRINT USER DIAGRAM", input);
		}
		Pattern pattern = Pattern.compile(PrintUserDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析显示注册用户状态语句，取出数据表名
	 * @param input 输入语句
	 * @return 返回PrintUserDiagram命令
	 */
	public PrintUserDiagram split(String input) {
		Pattern pattern = Pattern.compile(PrintUserDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		PrintUserDiagram cmd = new PrintUserDiagram();
		cmd.setPrimitive(input); // 保存原语

		String text = matcher.group(1);
		// 不是ME关键字，只显示自己
		boolean match = text.matches("^\\s*(?i)(ME)\\s*$");
		if (match) {
			return cmd;
		}

		// 按照逗号，分割字符串，逐一解析
		String[] items = splitCommaSymbol(text);
		for (String item : items) {
			// 生成一个用户签名
			Siger siger = splitSiger(item);
			cmd.addUser(siger);
			cmd.addPlainText(item);
		}

		return cmd;
	}

}