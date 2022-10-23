/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.access.user.*;
import com.laxcus.util.tip.*;

/**
 * 检索用户分布站点解析器
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekUserSiteParser extends SeekUserResourceParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SEEK\\s+USER\\s+SITE)\\s+([\\w\\W]+?)\\s+(?i)(?:ON)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的检索用户分布站点解析器
	 */
	public SeekUserSiteParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SEEK USER SITE", input);
		}
		Pattern pattern = Pattern.compile(SeekUserSiteParser.REGEX);
		Matcher matcher = pattern.matcher(input);		
		return matcher.matches();
	}

	/**
	 * 解析检索用户分布站点命令
	 * @param input 输入语句
	 * @return 输出命令
	 */
	public SeekUserSite split(String input) {
		Pattern pattern = Pattern.compile(SeekUserSiteParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String prefix = matcher.group(1);
		String suffix = matcher.group(2);

		SeekUserSite cmd = new SeekUserSite();

		// 解析命令
		split(prefix, suffix, cmd);
		// 设置原语
		cmd.setPrimitive(input);
		return cmd;
	}

}