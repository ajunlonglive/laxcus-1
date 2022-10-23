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

import com.laxcus.command.site.gate.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 检查GATE站点的注册用户和站点编号的一致性解析器。<br>
 * 
 * 语法：CHECK SHADOW CONSISTENCY TO ALL | GATE地址,GATE地址
 * 
 * @author scott.liang
 * @version 1.0 7/20/2019
 * @since laxcus 1.0
 */
public class CheckShadowConsistencyParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+SHADOW\\s+CONSISTENCY\\s+TO)\\s+(ALL|[\\w\\W]+)\\s*$";

	/**
	 * 构造检查GATE站点的注册用户和站点编号的一致性解析器
	 */
	public CheckShadowConsistencyParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("CHECK SHADOW CONSISTENCY", input);
		}
		Pattern pattern = Pattern.compile(CheckShadowConsistencyParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“CHECK SHADOWN CONSISTENCY”命令
	 * @param input 输入语句
	 * @return 返回CheckShadowConsistency命令
	 */
	public CheckShadowConsistency split(String input) {
		CheckShadowConsistency cmd = new CheckShadowConsistency();
		cmd.setPrimitive(input);

		Pattern pattern = Pattern.compile(CheckShadowConsistencyParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// 解析地址
		String suffix = matcher.group(1);
		// 不是全部
		if (!suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
			List<Node> sites = splitSites(suffix, SiteTag.GATE_SITE);
			cmd.addAll(sites);
		}

		return cmd;
	}

}