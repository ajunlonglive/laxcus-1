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

import com.laxcus.command.mix.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 打印节点检测目录解析器。<br><br>
 * 
 * 语法格式：CHECK SITE PATH TO LOCAL|ALL|节点地址 ... <br>
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public class CheckSitePathParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+SITE\\s+PATH\\s+TO)\\s+(.+?)\\s*$";

	/**
	 * 构造默认的打印节点检测目录解析器
	 */
	public CheckSitePathParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK SITE PATH", input);
		}
		Pattern pattern = Pattern.compile(CheckSitePathParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析打印节点检测目录语句，取出参数
	 * @param input 输入语句
	 * @return 返回CheckSitePath命令
	 */
	public CheckSitePath split(String input) {
		Pattern pattern = Pattern.compile(CheckSitePathParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		CheckSitePath cmd = new CheckSitePath();
		cmd.setPrimitive(input); // 保存原语

		// 目标地址
		String suffix = matcher.group(1);
		// 判断是“LOCAL”、“ALL”关键字，或者其它节点地址
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		return cmd;
	}

}