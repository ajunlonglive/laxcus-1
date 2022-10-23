/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;
import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 设置调用器数目解析器 <BR><BR>
 * 
 * 格式：SET MAX INVOKER 队列成员数 TO [节点地址|ALL|LOCAL]
 * 
 * @author scott.liang
 * @version 1.0 9/11/2020
 * @since laxcus 1.0
 */
public class MaxInvokerParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+MAX\\s+INVOKER)\\s+(?i)([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/** 成员数目 **/
	private final static String MEMBER = "^\\s*(?i)(?:-M)\\s+([\\w\\W]+?)(?i)(\\s*|\\s+-CT\\s+[\\w\\W]+)\\s*$";
	
	/** 延迟时间 **/
	private final static String CONFINE_TIME = "^\\s*(?i)(?:-CT)\\s+([\\w\\W]+?)(?i)(\\s*|\\s+-M\\s+[\\w\\W]+)\\s*$";
	
	/**
	 * 构造默认的设置调用器数目解析器
	 */
	public MaxInvokerParser() {
		super();
	}
	
	/**
	 * 判断匹配“SET MAX INVOKER”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET MAX INVOKER", input);
		}
		Pattern pattern = Pattern.compile(MaxInvokerParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析参数
	 * @param input
	 * @param cmd
	 */
	private void splitParams(String input, MaxInvoker cmd) {
		while (input.trim().length() > 0) {
			// 成员
			Pattern pattern = Pattern.compile(MaxInvokerParser.MEMBER);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String sub = matcher.group(1);
				input = matcher.group(2);
				cmd.setInvokers(Integer.parseInt(sub));
				continue;
			}
			// 具体的限制时间
			pattern = Pattern.compile(MaxInvokerParser.CONFINE_TIME);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String sub = matcher.group(1);
				input = matcher.group(2);
				// 解析时间
				if (sub.equalsIgnoreCase("ALWAYS")) {
					cmd.setConfineTime(0); // 时间是0，总是限制
				} else {
					long ms = ConfigParser.splitTime(sub, -1);
					if (ms < 0) {
						throwableNo(FaultTip.INCORRECT_SYNTAX_X, sub);
					}
					cmd.setConfineTime(ms);
				}
				continue;
			}
			// 弹出异常
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
	}

	/**
	 * 解析“SET MAX INVOKER”语句
	 * @param input 输入语句
	 * @return 返回SetMaxInvoker命令
	 */
	public MaxInvoker split(String input) {
		Pattern pattern = Pattern.compile(MaxInvokerParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		MaxInvoker cmd = new MaxInvoker();
	
		// 参数
		String params = matcher.group(1);
		splitParams(params, cmd);
		
		// 目标地址
		String suffix = matcher.group(2);
		// 参数
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}