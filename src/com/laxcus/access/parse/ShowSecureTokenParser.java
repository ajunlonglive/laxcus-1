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

import com.laxcus.command.secure.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 显示密钥令牌解析器 <BR><BR>
 * 
 * 格式: SHOW SECURE TOKEN FROM [ 节点地址 ]
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class ShowSecureTokenParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SHOW\\s+SECURE\\s+TOKEN\\s+FROM)\\s([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的显示密钥令牌解析器
	 */
	public ShowSecureTokenParser() {
		super();
	}
	
	/**
	 * 判断匹配“SHOW SECURE TOKEN”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SHOW SECURE TOKEN", input);
		}
		Pattern pattern = Pattern.compile(ShowSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SHOW SECURE TOKEN”语句
	 * @param input 输入语句
	 * @return 返回ReloadSecure命令
	 */
	public ShowSecureToken split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ShowSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ShowSecureToken cmd = new ShowSecureToken();

		String suffix = matcher.group(1);
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}