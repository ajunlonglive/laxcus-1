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

import com.laxcus.command.shutdown.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 远程关闭运行站点解析器
 * 
 * @author scott.liang
 * @version 1.1 12/28/2012
 * @since laxcus 1.0
 */
public class ShutdownParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String SHUTDOWN1 = "^\\s*(?i)(?:SHUTDOWN)\\s+([\\w\\W]+)\\s+(?i)(?:-DELAY|-D)\\s+([\\w\\W]+)\\s*$"; // "^\\s*(?i)(?:SHUTDOWN)\\s+([\\w\\W]+)\\s+(?i)(?:DELAY)\\s+([\\w\\W]+)\\s*$";
	private final static String SHUTDOWN2 = "^\\s*(?i)(?:SHUTDOWN)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造远程关闭运行站点解析器
	 */
	public ShutdownParser() {
		super();
	}

	/**
	 * 检查匹配“SHUTDOWN ...”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SHUTDOWN", input);
		}
		Pattern pattern = Pattern.compile(ShutdownParser.SHUTDOWN1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(ShutdownParser.SHUTDOWN2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 解析命令“SHUTDOWN ...”
	 * @param input 语法
	 * @return 返回SHUTDOWN命令
	 */
	public Shutdown split(String input) {
		Shutdown cmd = new Shutdown();
		String delay = null;
		String params = null;

		// 第一个选项
		Pattern pattern = Pattern.compile(ShutdownParser.SHUTDOWN1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			params = matcher.group(1);
			delay = matcher.group(2);
		}
		// 第二个选项
		if (!success) {
			pattern = Pattern.compile(ShutdownParser.SHUTDOWN2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				params = matcher.group(1);
			}
		}
		// 以上不成功，弹出错误
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 延时时间
		if (delay != null) {
			if (!ConfigParser.isTime(delay)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, delay);
			}
			// 解析时间
			long ms = ConfigParser.splitTime(delay, -1);
			cmd.setDelay(ms);
		}

		// 如果不是全部
		if (!params.matches("^\\s*(?i)(ALL)\\s*$")) {
			List<Node> nodes = splitSites(params);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input); // 原语

		// 返回命令
		return cmd;
	}

}

//	/**
//	 * 检查匹配“SHUTDOWN ...”语句
//	 * @param input 输入语句
//	 * @return 返回真或者假
//	 */
//	public boolean matches(String input) {
//		Pattern pattern = Pattern.compile(ShutdownParser.SHUTDOWN1);
//		Matcher matcher = pattern.matcher(input);
//		boolean match = matcher.matches();
//		if (!match) {
//			pattern = Pattern.compile(ShutdownParser.SHUTDOWN2);
//			matcher = pattern.matcher(input);
//			match = matcher.matches();
//		}
//		return match;
//	}
//	
//	/**
//	 * 解析命令“SHUTDOWN ...”
//	 * @param input 语法
//	 * @return 返回SHUTDOWN命令
//	 */
//	public Shutdown split(String input) {
//		Shutdown cmd = new Shutdown();
//		cmd.setPrimitive(input); // 原语
//
//		// 第一个选项
//		Pattern pattern = Pattern.compile(ShutdownParser.SHUTDOWN1);
//		Matcher matcher = pattern.matcher(input);
//		if (matcher.matches()) {
//			cmd.setAll(true);
//			return cmd;
//		}
//		
//		String suffix = matcher.group(1);
//		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
//			List<Node> nodes = splitSites(suffix);
//			cmd.addAll(nodes);
//		}
//
//		// 第二种选项
//		pattern = Pattern.compile(ShutdownParser.SHUTDOWN2);
//		matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//
//		// 解析全部站点
//		String line = matcher.group(1);
//		List<Node> sites = splitSites(line);
//		for (Node e : sites) {
//			cmd.add(e);
//		}
//		// 返回命令
//		return cmd;
//	}