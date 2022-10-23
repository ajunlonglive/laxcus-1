/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.util.tip.*;

/**
 * 检查集成分布成员解析器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2020
 * @since laxcus 1.0
 */
public class CheckDistributedMemberParser extends MultiUserParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+DISTRIBUTED\\s+MEMBER)\\s+(ALL|[\\w\\W]+)\\s*$";

	/**
	 * 构造默认的检查集成分布成员解析器
	 */
	public CheckDistributedMemberParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("CHECK DISTRIBUTED MEMBER", input);
		}
		Pattern pattern = Pattern.compile(CheckDistributedMemberParser.REGEX);
		Matcher matcher = pattern.matcher(input);		
		return matcher.matches();
	}

	/**
	 * 解析发布用户命令
	 * @param input 输入语句
	 * @return 输出命令
	 */
	public CheckDistributedMember split(String input) {
		Pattern pattern = Pattern.compile(CheckDistributedMemberParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 用户
		String prefix = matcher.group(1);

		// 命令
		CheckDistributedMember cmd = new CheckDistributedMember();
		
		// 不是ALL关键字
		if (!prefix.matches("^\\s*(?i)(ALL)\\s*$")) {
			// 解析用户签名
			splitSigers(prefix, cmd);
		}

		// 设置原语
		cmd.setPrimitive(input);
		return cmd;
	}

}