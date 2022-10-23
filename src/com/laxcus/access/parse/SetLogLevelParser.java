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
import com.laxcus.util.tip.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 设置日志级别解析器 <br><br>
 * 
 * 格式： SET LOG LEVEL [DEBUG|INFO|INFOR|INFORMATION|WARNING|ERROR|FATAL] TO [ALL| sites ... ]
 * 
 * @author scott.liang
 * @version 1.0 8/16/2017
 * @since laxcus 1.0
 */
public class SetLogLevelParser extends SyntaxParser {

	private final static String REGEX = "^\\s*(?i)(?:SET\\s+LOG\\s+LEVEL)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的设置日志级别解析器
	 */
	public SetLogLevelParser() {
		super();
	}

	/**
	 * 判断匹配“SET LOG LEVEL”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET LOG LEVEL", input);
		}
		Pattern pattern = Pattern.compile(SetLogLevelParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SET LOG LEVEL”语句
	 * @param input 输入语句
	 * @return 返回SetLogLevel命令
	 */
	public SetLogLevel split(String input) {
		Pattern pattern = Pattern.compile(SetLogLevelParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		SetLogLevel cmd = new SetLogLevel();

		String tag = matcher.group(1);
		String sites = matcher.group(2);

		// 1. 取日志级别
		int level = LogLevel.translate(tag);
		if (!LogLevel.isLevel(level)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, tag);
		}
		cmd.setLevel(level);

		// 2.取站点地址
		if (!sites.matches("^\\s*(?i)(ALL)\\s*$")) {
			List<Node> nodes = splitSites(sites);
			cmd.addAll(nodes);
		}
		// 保存命令原语
		cmd.setPrimitive(input);
		// 返回命令
		return cmd;
	}

}