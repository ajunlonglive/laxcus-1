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

import com.laxcus.command.reload.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 重新设置节点的安全策略解析器 <BR><BR>
 * 
 * 格式：RELOAD SECURITY POLICY TO [节点地址|ALL]
 * 
 * @author scott.liang
 * @version 1.0 6/17/2018
 * @since laxcus 1.0
 */
public class ReloadSecurityPolicyParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:RELOAD\\s+SECURITY\\s+POLICY\\s+TO)\\s([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的重新设置节点的安全策略解析器
	 */
	public ReloadSecurityPolicyParser() {
		super();
	}
	
	/**
	 * 判断匹配“RELOAD SECURITY POLICY”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RELOAD SECURITY POLICY", input);
		}
		Pattern pattern = Pattern.compile(ReloadSecurityPolicyParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RELOAD SECURITY POLICY”语句
	 * @param input 输入语句
	 * @return 返回ReloadSecurityPolicy命令
	 */
	public ReloadSecurityPolicy split(String input) {
		Pattern pattern = Pattern.compile(ReloadSecurityPolicyParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReloadSecurityPolicy cmd = new ReloadSecurityPolicy();

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
