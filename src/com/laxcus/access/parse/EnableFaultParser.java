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

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 生效故障通知解析器 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/26/2019
 * @since laxcus 1.0
 */
public class EnableFaultParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:OPEN\\s+FAULT\\s+MESSAGE)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造生效故障通知解析器
	 */
	public EnableFaultParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("OPEN FAULT MESSAGE", input);
		}
		Pattern pattern = Pattern.compile(EnableFaultParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析生效故障通知语句
	 * @param input 输入语句
	 * @return 返回EnableFault命令
	 */
	public EnableFault split(String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(EnableFaultParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		EnableFault cmd = new EnableFault();
		// 目标地址
		String suffix = matcher.group(1);
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
			cmd.setAll(true);
		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		// 保存命令原语
		cmd.setPrimitive(input);
		return cmd;
	}

}