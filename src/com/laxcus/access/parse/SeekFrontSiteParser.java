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

import com.laxcus.command.site.front.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 检查FRONT在线用户解析器。<br>
 * 
 * 语法：
 * 1. SEEK FRONT SITE TO ALL
 * 2. SEEK FRONT SITE TO GATE/CALL节点地址
 * 
 * @author scott.liang
 * @version 1.0 09/09/2012
 * @since laxcus 1.0
 */
public class SeekFrontSiteParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SEEK\\s+FRONT\\s+SITE\\s+TO)\\s+(ALL|[\\w\\W]+)\\s*$";

	/**
	 * 构造检查FRONT在线用户解析器
	 */
	public SeekFrontSiteParser() {
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
			return isCommand("SEEK FRONT SITE", input);
		}
		Pattern pattern = Pattern.compile(SeekFrontSiteParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SEEK FRONT SITE”命令
	 * @param input 输入语句
	 * @return 返回SeekFrontSite命令
	 */
	public SeekFrontSite split(String input) {
		SeekFrontSite cmd = new SeekFrontSite();
		cmd.setPrimitive(input);

		Pattern pattern = Pattern.compile(SeekFrontSiteParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}

		// 解析地址
		String suffix = matcher.group(1);
		// 不是全部
		if (!suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
			// 检索CALL/GATE两种类型的站点
			byte[] types = new byte[] { SiteTag.CALL_SITE, SiteTag.GATE_SITE };
			// 检查
			List<Node> sites = splitSites(suffix, types);
			cmd.addAll(sites);
		}

		return cmd;
	}

}