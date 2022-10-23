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
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 打印数据表图谱命令解析器。<br><br>
 * 
 * 语法格式：PRINT TABLE DIAGRAM ALL | 数据库.表, 数据库.表 FROM ME|用户签名, ... <br>
 * 
 * @author scott.liang
 * @version 1.0 5/12/2013
 * @since laxcus 1.0
 */
public class PrintTableDiagramParser extends SyntaxParser {
	
	/** 标题 **/
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:PRINT\\s+TABLE\\s+DIAGRAM)\\s+([\\w\\W]+)\\s*$";
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PRINT\\s+TABLE\\s+DIAGRAM)\\s+(ALL|[\\w\\W]+)\\s+(?i)(?:FROM)\\s+(?i)(ME|[\\w\\W]+)\\s*$";

	/**
	 * 构造默认的打印数据表图谱命令解析器
	 */
	public PrintTableDiagramParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("PRINT TABLE DIAGRAM", input);
		}
		Pattern pattern = Pattern.compile(PrintTableDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析打印数据表图谱语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回PrintTableDiagram命令
	 */
	public PrintTableDiagram split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(PrintTableDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		PrintTableDiagram cmd = new PrintTableDiagram();
		cmd.setPrimitive(input); // 保存原语

		String text = matcher.group(1);
		// 不是ALL关键字，检查全部表
		boolean all = text.matches("^\\s*(?i)(?:ALL)\\s*$");

		// 按照逗号，分割字符串，逐一解析
		if (!all) {
			String[] items = splitCommaSymbol(text);
			for (String item : items) {
				// 判断表名正确
				if (!Space.validate(item)) {
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
		}

		// 用户签名
		text = matcher.group(2);
		boolean me = text.matches("^\\s*(?i)(?:ME)\\s*$");
		if(!me) {
			// 按照逗号，分割字符串，逐一解析
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

//	/**
//	 * 解析打印数据表图谱语句，取出数据表名
//	 * @param input 输入语句
//	 * @param online 在线状态
//	 * @return 返回PrintTableDiagram命令
//	 */
//	public PrintTableDiagram split2(String input, boolean online) {
//		Pattern pattern = Pattern.compile(PrintTableDiagramParser.REGEX);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX);
//		}
//
//		PrintTableDiagram cmd = new PrintTableDiagram();
//		cmd.setPrimitive(input); // 保存原语
//
//		String text = matcher.group(1);
//		// 不是ALL关键字，检查全部数据库
//		boolean match = text.matches("^\\s*(?i)(?:ALL)\\s*$");
//		if (match) {
//			return cmd;
//		}
//
//		// 按照逗号，分割字符串，逐一解析
//		String[] items = splitCommaSymbol(text);
//		for (String item : items) {
//			// 判断表名正确
//			if(!Space.validate(input)) {
//				throwable(FaultTip.INCORRECT_SYNTAX_X, item);
//			}
//			Space space = new Space(item);
//			// 如果是在线模式，检查这个数据表
//			if (online) {
//				if (!hasTable(space)) {
//					throwable(FaultTip.NOTFOUND_X, space);
//				}
//			}
//			cmd.add(space);
//		}
//
//		return cmd;
//	}	

}
