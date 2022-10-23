/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.site.front.*;
import com.laxcus.util.tip.*;

/**
 * 检索工作节点解析器。<br><br>
 * 
 * 语法格式：<br>
 * 1. CHECK JOB SITE  <br>
 * 
 * @author scott.liang
 * @version 1.0 6/5/2020
 * @since laxcus 1.0
 */
public class CheckJobSiteParser extends SyntaxParser {

	/** 检索登录和注册节点命令集合 **/
	private final static String REGEX1 = "^\\s*(?i)(?:CHECK\\s+JOB\\s+SITE)\\s*$";

	/**
	 * 构造检索登录和注册节点解析器
	 */
	public CheckJobSiteParser() {
		super();
	}

	/**
	 * 判断语句匹配“CHECK REMOTE SITE”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(CheckJobSiteParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“CHECK JOB SITE ...”命令
	 * @param input 输入语句
	 * @return 返回CheckJobSite命令
	 */
	public CheckJobSite split(String input) {
		// 判断正确
		Pattern pattern = Pattern.compile(CheckJobSiteParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		CheckJobSite cmd = new CheckJobSite();

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}