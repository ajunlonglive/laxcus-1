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
 * 输出密钥令牌解析器 <BR><BR>
 * 
 * 格式: FLUSH SECURE TOKEN TO [ 节点地址 ]
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class FlushSecureTokenParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:FLUSH\\s+SECURE\\s+TOKEN\\s+TO)\\s([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的输出密钥令牌解析器
	 */
	public FlushSecureTokenParser() {
		super();
	}
	
	/**
	 * 判断匹配“FLUSH SECURE TOKEN”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("FLUSH SECURE TOKEN", input);
		}
		Pattern pattern = Pattern.compile(FlushSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“FLUSH SECURE TOKEN”语句
	 * @param input 输入语句
	 * @return 返回ReloadSecure命令
	 */
	public FlushSecureToken split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(FlushSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		FlushSecureToken cmd = new FlushSecureToken();

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