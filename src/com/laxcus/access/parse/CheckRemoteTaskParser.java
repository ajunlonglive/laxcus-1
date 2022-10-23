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

import com.laxcus.command.site.front.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 检测本地组件解析器。<br><br>
 * 
 * 语法格式1：CHECK REMOTE TASK 组件, 组件, ... <br>
 * 语法格式2：CHECK REMOTE TASK ALL <br>
 * 
 * @author scott.liang
 * @version 1.0 11/07/2018
 * @since laxcus 1.0
 */
public class CheckRemoteTaskParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+REMOTE\\s+TASK)(\\s+[\\w\\W]+|\\s*)$";

	private final static String SUFFIX = "^\\s*([\\w\\W]*?)\\s+(?i)(?:-FULL|-F)\\s+([\\w\\W]+)\\s*$";
	
	/**
	 * 构造默认的检测本地组件解析器
	 */
	public CheckRemoteTaskParser() {
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
			return isCommand("CHECK REMOTE TASK", input);
		}
		Pattern pattern = Pattern.compile(CheckRemoteTaskParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析组件根命名
	 * @param suffix 输入语句
	 * @return 返回命名列表
	 */
	private List<Sock> splitRoots(String suffix) {
		ArrayList<Sock> a = new ArrayList<Sock>();

		// 按照逗号，分割字符串，逐一解析
		String[] items = splitCommaSymbol(suffix);
		for (String item : items) {
			// 不是标准格式，弹出异常
			if (!Sock.validate(item)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			// 生成命名实例
			Sock e = new Sock(item);
			// 不存在，保存它
			if (!a.contains(e)) {
				a.add(e);
			}
		}

		return a;
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回CheckRemoteTask命令
	 */
	public CheckRemoteTask split(String input, boolean online) {
		Pattern pattern = Pattern.compile(CheckRemoteTaskParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		CheckRemoteTask cmd = new CheckRemoteTask();
		cmd.setPrimitive(input); // 保存原语

		String text = matcher.group(1);
		pattern = Pattern.compile(CheckRemoteTaskParser.SUFFIX);
		matcher = pattern.matcher(text);
		// 判断匹配，取出前面的参数
		boolean match = matcher.matches();
		String suffix = (match ? matcher.group(1) : text);
		// 判断有参数
		if (suffix.trim().length() > 0) {
			List<Sock> roots = splitRoots(suffix);
			if (roots == null || roots.isEmpty()) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}
			cmd.addAll(roots);
		}

		// 解析后面的参数
		if (match) {
			boolean full = ConfigParser.splitBoolean(matcher.group(2), false);
			cmd.setFull(full);
		}

		return cmd;
	}	
	
//	/**
//	 * 解析显示表语句，取出数据表名
//	 * @param input 输入语句
//	 * @param online 在线状态
//	 * @return 返回CheckRemoteTask命令
//	 */
//	public CheckRemoteTask split(String input, boolean online) {
//		Pattern pattern = Pattern.compile(CheckRemoteTaskParser.REGEX);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX);
//		}
//
//		CheckRemoteTask cmd = new CheckRemoteTask();
//		cmd.setPrimitive(input); // 保存原语
//
//		String suffix = matcher.group(1);
//		// 判断有参数
//		if (suffix.trim().length() > 0) {
//			List<Sock> a = splitRoots(suffix);
//			cmd.addAll(a);
//		}
//
//		return cmd;
//	}	

}