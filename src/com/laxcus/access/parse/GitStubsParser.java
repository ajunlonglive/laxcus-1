/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;

/**
 * 获得数据块编号解析器。<br><br>
 * 
 * 语法格式：GIT ENTITY STUBS 数据库.表 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/10/2018
 * @since laxcus 1.0
 */
public class GitStubsParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:GIT\\s+ENTITY\\s+STUBS)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的获得数据块编号解析器
	 */
	public GitStubsParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("GIT ENTITY STUBS", input);
		}
		Pattern pattern = Pattern.compile(GitStubsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回GitStubs命令
	 */
	public GitStubs split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(GitStubsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		String text = matcher.group(1);
		// 判断表名有效
		if (!Space.validate(text)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, text);
		}

		// 生成数据表名
		Space space = new Space(text);
		// 在线检查表
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, text);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		GitStubs cmd = new GitStubs(space);
		cmd.setPrimitive(input); // 保存原语
		return cmd;
	}

}
