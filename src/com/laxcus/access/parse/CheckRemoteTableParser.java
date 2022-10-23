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
import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;

/**
 * 检测本地注册表解析器。<br><br>
 * 
 * 语法格式1：CHECK REMOTE TABLE 数据库.表, 数据库.表, ... <br>
 * 语法格式2：CHECK REMOTE TABLE  <br>
 * 
 * @author scott.liang
 * @version 1.0 11/07/2018
 * @since laxcus 1.0
 */
public class CheckRemoteTableParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+REMOTE\\s+TABLE)(\\s+[\\w\\W]+|\\s*)$";

	/**
	 * 构造默认的检测本地注册表解析器
	 */
	public CheckRemoteTableParser() {
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
			return isCommand("CHECK REMOTE TABLE", input);
		}
		Pattern pattern = Pattern.compile(CheckRemoteTableParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析数据表
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回数据表列表
	 */
	private List<Space> splitTables(String input, boolean online) {
		ArrayList<Space> a = new ArrayList<Space>();
		// 按照逗号，分割字符串，逐一解析
		String[] items = splitCommaSymbol(input);
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
			// 不存在，保存它！
			if (!a.contains(space)) {
				a.add(space);
			}
		}
		return a;
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回CheckRemoteTable命令
	 */
	public CheckRemoteTable split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(CheckRemoteTableParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		CheckRemoteTable cmd = new CheckRemoteTable();
		cmd.setPrimitive(input); // 保存原语

		// 解析参数
		String suffix = matcher.group(1);
		if (suffix.trim().length() > 0) {
			List<Space> a = splitTables(suffix, online);
			cmd.addAll(a);
		}
		
//		// 不是ALL关键字，检查全部数据库
//		boolean match = suffix.matches("^\\s*(?i)(?:ALL)\\s*$");
//		if (match) {
//			return cmd;
//		}
//		// 按照逗号，分割字符串，逐一解析
//		String[] items = splitCommaSymbol(text);
//		for (String item : items) {
//			// 判断表名正确
//			if(!Space.validate(item)) {
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

		return cmd;
	}

}