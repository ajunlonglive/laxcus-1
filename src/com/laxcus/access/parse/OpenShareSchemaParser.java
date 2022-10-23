/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.cross.*;
import com.laxcus.util.tip.*;

/**
 * 开放数据库共享解析器 <br>
 * 只在FRONT站点，由资源持有人使用 <br><br>
 * 
 * 语法格式：
 * OPEN SHARE DATABASE [ALL | 数据库名1, 数据库名2, ...] ON [SELECT, INSERT, DELETE, UPDATE] TO [用户名 | SIGN 用户签名 , ...]
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public class OpenShareSchemaParser extends ShareSchemaParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:OPEN\\s+SHARE\\s+DATABASE)\\s+([\\w\\W]+)\\s*$";
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:OPEN\\s+SHARE\\s+DATABASE)\\s+([\\w\\W]+?)\\s+(?i)(?:ON)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$"; 

	/**
	 * 构造默认的开放数据库共享解析器
	 */
	public OpenShareSchemaParser() {
		super();
	}

	/**
	 * 判断匹配“OPEN SHARE DATABASE”命令
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("OPEN SHARE DATABASE", input);
		}
		Pattern pattern = Pattern.compile(OpenShareSchemaParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析OPEN SHARE DATABASE”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回OpenShareSchema命令
	 */
	public OpenShareSchema split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(OpenShareSchemaParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 判断允许执行开放资源操作
		if (online) {
			if (!canUser(ControlTag.OPEN_RESOURCE)) {
				throwable(FaultTip.PERMISSION_MISSING);
			}
		}
		
		// 解析路径
		String databases = matcher.group(1);
		String operator = matcher.group(2);
		String names = matcher.group(3);
		
		// 命令
		OpenShareSchema cmd = new OpenShareSchema();
		// 解析参数
		split(cmd, online, databases, operator, names);
		// 命令原语
		cmd.setPrimitive(input);

		// 返回结果
		return cmd;
	}

}