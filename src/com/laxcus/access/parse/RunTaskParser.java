/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.cloud.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 运行分布式应用软件解析器 <br>
 * 
 * 语法：RUN DAPP 基础字
 * 
 * @author scott.liang
 * @version 1.0 8/29/2020
 * @since laxcus 1.0
 */
public class RunTaskParser extends SyntaxParser {

	/** 命令语句 **/
	private final static String REGEX = "^\\s*(?i)(?:RUN\\s+DAPP)\\s+([\\w\\W]+?)\\s*$";

	/** 基础字， 由软件名称和组件根名称组成，软件名称限制16个字符 **/
	private final static String SOCK = "^\\s*(?i)([\\w\\W]{1,16}?)\\.([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的运行分布式应用软件解析器
	 */
	public RunTaskParser() {
		super();
	}

	/**
	 * 检查传入的参数是否匹配"RUN DAPP"语句
	 * @param simple 简单判断
	 * @param input  输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RUN DAPP", input);
		}
		Pattern pattern = Pattern.compile(RunTaskParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析任务应用名称，包括软件名和组件名
	 * @param input 输入语句
	 * @return 返回Sock
	 * @throws SyntaxException 如果语句错误时...
	 */
	private Sock splitSock(String input) {
		Pattern pattern = Pattern.compile(RunTaskParser.SOCK);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 软件名称和组件名称
		String ware = matcher.group(1);
		String root = matcher.group(2);
		return new Sock(ware, root);
	}

	/**
	 * 解析运行分布应用软件侠侣 
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回RunTask命令
	 */
	public RunTask split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(RunTaskParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，弹出异常
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 解析基础字
		Sock sock = splitSock(matcher.group(1));

		// 生成命令
		RunTask cmd = new RunTask(sock);
		cmd.setPrimitive(input);
		return cmd;
	}

}