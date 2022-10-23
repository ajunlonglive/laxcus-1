/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.net.*;
import java.util.regex.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 检索站点在线资源解析器 <br>
 * 
 * 被管理员使用，从WATCH站点发出，目标是所有节点。
 * 
 * @author scott.liang
 * @version 1.0 4/22/2018
 * @since laxcus 1.0
 */
public class SeekOnlineResourceParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SEEK\\s+ONLINE\\s+RESOURCE\\s+TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造检索站点在线资源解析器
	 */
	public SeekOnlineResourceParser() {
		super();
	}

	/**
	 * 判断匹配被WATCH监视节点的定时刷新语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SEEK ONLINE RESOURCE", input);
		}
		Pattern pattern = Pattern.compile(SeekOnlineResourceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析被WATCH监视节点的定时刷新语句
	 * @param input 输入语句
	 * @return 返回SeekOnlineResource命令
	 */
	public SeekOnlineResource split(String input) {
		Pattern pattern = Pattern.compile(SeekOnlineResourceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String syntax = matcher.group(1);
		// 判断语法正确
		if (!Node.validate(syntax)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, syntax);
		}

		SeekOnlineResource cmd = new SeekOnlineResource();
		try {
			Node node = new Node(syntax);
			cmd.setSite(node);
		} catch (UnknownHostException e) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, syntax);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}