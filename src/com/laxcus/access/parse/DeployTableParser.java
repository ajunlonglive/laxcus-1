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

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;
import com.laxcus.site.*;

/**
 * 部署数据表到指定的站点解析器
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class DeployTableParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:DEPLOY\\s+TABLE)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的部署数据表到指定的站点解析器
	 */
	public DeployTableParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DEPLOY TABLE", input);
		}
		Pattern pattern = Pattern.compile(DeployTableParser.REGEX);
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
	 * 解析部署数据表命令
	 * @param input 输入语句
	 * @return 输出命令
	 */
	public DeployTable split(String input) {
		Pattern pattern = Pattern.compile(DeployTableParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 数据表
		String prefix = matcher.group(1);
		// 站点
		String suffix = matcher.group(2);

		// 判断表名正确
		if (!Space.validate(prefix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Space space = new Space(prefix);
		// 命令
		DeployTable cmd = new DeployTable(space);

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
