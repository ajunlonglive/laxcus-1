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

import com.laxcus.command.site.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 强制节点重新注册解析器 <br>
 * 
 * 语法格式：REFRESH LOGIN TO [ALL|节点地址1, ....]
 * 
 * @author scott.liang
 * @version 1.0 5/12/2017
 * @since laxcus 1.0
 */
public class RefreshLoginParser extends SyntaxParser {
	
	/** 命令语句 **/
	private final static String REGEX = "^\\s*(?i)(?:REFRESH\\s+LOGIN\\s+TO)\\s+(?i)(ALL|[\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的强制节点重新注册解析器
	 */
	public RefreshLoginParser() {
		super();
	}

	/**
	 * 判断匹配“REFRESH LOGIN TO”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("REFRESH LOGIN", input);
		}
		Pattern pattern = Pattern.compile(RefreshLoginParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析强制节点重新注册“REFRESH LOGIN TO”
	 * @param input 输入语句
	 * @return 返回RefreshLogin命令
	 */
	public RefreshLogin split(String input) {
		Pattern pattern = Pattern.compile(RefreshLoginParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 参数
		String suffix = matcher.group(1);

		// 生成命令
		RefreshLogin cmd = new RefreshLogin();

		// 不要求全部时，解析每个站点地址
		if (!suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
			List<Node> sites = splitSites(suffix);
			cmd.addAll(sites);
		}
		
		// 返回命令
		cmd.setPrimitive(input);
		return cmd;
	}

}