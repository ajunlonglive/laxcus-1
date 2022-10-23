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
 * 检索分布任务组件解析器。<br><br>
 * 
 * 语法格式：<br>
 * 1. CHECK LOCAL TASK  <br>
 * 2. CHECK LOCAL TASK [TASK NAME, ...]<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/20/2019
 * @since laxcus 1.0
 */
public class CheckLocalTaskParser extends SyntaxParser {

	/** 检索分布任务组件命令集合 **/
	private final static String REGEX1 = "^\\s*(?i)(?:CHECK\\s+LOCAL\\s+TASK)(\\s+[\\w\\W]+|\\s*)$";

	private final static String SUFFIX = "^\\s*([\\w\\W]*?)\\s+(?i)(?:-FULL|-F)\\s+([\\w\\W]+)\\s*$";
	
	/**
	 * 构造检索分布任务组件解析器
	 */
	public CheckLocalTaskParser() {
		super();
	}

	/**
	 * 判断语句匹配“CHECK LOCAL TASK”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK LOCAL TASK", input);
		}
		Pattern pattern = Pattern.compile(CheckLocalTaskParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析分布任务组件根命名
	 * @param input 输入参数
	 * @return 列表
	 */
	private List<Sock> splitSocks(String input) {
		ArrayList<Sock> array = new ArrayList<Sock>();
		String[] items = splitCommaSymbol(input);
		for (String item : items) {
			// 判断是标准命名格式，不匹配，弹出错误
			if (!Sock.validate(item)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			// 保存阶段命名
			Sock e = new Sock(item);
			if (!array.contains(e)) {
				array.add(e);
			}
		}
		return new ArrayList<Sock>(array);
	}

	/**
	 * 解析“CHECK LOCAL TASK ...”命令
	 * @param input 输入语句
	 * @return 返回CheckLocalTask命令
	 */
	public CheckLocalTask split(String input) {
		// 判断正确
		Pattern pattern = Pattern.compile(CheckLocalTaskParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		CheckLocalTask cmd = new CheckLocalTask();
		// 保存命令原语
		cmd.setPrimitive(input);

//		// 取出参数
//		String suffix = matcher.group(1);
//		// 判断有字符
//		if (suffix.trim().length() > 0) {
//			List<Sock> roots = splitSocks(suffix);
//			if (roots == null || roots.isEmpty()) {
//				throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//			}
//			cmd.addAll(roots);
//		}

		String text = matcher.group(1);
		// 再次比较
		pattern = Pattern.compile(CheckLocalTaskParser.SUFFIX);
		matcher = pattern.matcher(text);
		// 判断匹配，取出前面的参数
		boolean match = matcher.matches();
		String suffix = (match ? matcher.group(1) : text);
		// 判断有参数
		if (suffix.trim().length() > 0) {
			List<Sock> roots = splitSocks(suffix);
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
//	 * 解析“CHECK LOCAL TASK ...”命令
//	 * @param input 输入语句
//	 * @return 返回CheckLocalTask命令
//	 */
//	public CheckLocalTask split(String input) {
//		// 判断正确
//		Pattern pattern = Pattern.compile(CheckLocalTaskParser.REGEX1);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//
//		CheckLocalTask cmd = new CheckLocalTask();
//
//		// 取出参数
//		String suffix = matcher.group(1);
//		// 判断有字符
//		if (suffix.trim().length() > 0) {
//			List<Sock> roots = splitSocks(suffix);
//			if (roots == null || roots.isEmpty()) {
//				throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//			}
//			cmd.addAll(roots);
//		}
//
//		// 保存命令原语
//		cmd.setPrimitive(input);
//
//		return cmd;
//	}

}