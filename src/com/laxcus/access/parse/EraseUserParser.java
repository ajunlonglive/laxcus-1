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

import com.laxcus.command.access.user.*;
import com.laxcus.util.tip.*;
import com.laxcus.site.*;

/**
 * 从指定的站点撤销用户解析器
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class EraseUserParser extends MultiUserParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:ERASE\\s+USER)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的从指定的站点撤销用户解析器
	 */
	public EraseUserParser() {
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
			return isCommand("ERASE USER", input);
		}
		Pattern pattern = Pattern.compile(EraseUserParser.REGEX);
		Matcher matcher = pattern.matcher(input);		
		return matcher.matches();
	}

	/**
	 * 判断是允许的站点
	 * @param e 节点
	 * @return 返回真或者假
	 */
	private boolean allow(Node e) {
		switch (e.getFamily()) {
		case SiteTag.DATA_SITE:
		case SiteTag.CALL_SITE:
		case SiteTag.BUILD_SITE:
		case SiteTag.WORK_SITE:
			return true;
		}
		return false;
	}

	/**
	 * 解析检索用户分布站点命令
	 * @param input 输入语句
	 * @return 输出命令
	 */
	public EraseUser split(String input) {
		Pattern pattern = Pattern.compile(EraseUserParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 用户
		String prefix = matcher.group(1);
		// 站点
		String suffix = matcher.group(2);

		// 命令
		EraseUser cmd = new EraseUser();

		// 解析用户签名
		splitSigers(prefix, cmd);

		// 解析全部节点地址
		List<Node> sites = splitSites(suffix);
		// 逐一判断
		for (Node e : sites) {
			// 节点无效，弹出来它
			if (!allow(e)) {
				throwableNo(FaultTip.ILLEGAL_SITE_X, e);
			}
			cmd.addSite(e);
		}

		// 设置原语
		cmd.setPrimitive(input);
		return cmd;
	}
}
