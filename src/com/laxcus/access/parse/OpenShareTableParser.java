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
 * 开放数据表共享解析器 <br>
 * 只在FRONT站点，由资源持有人使用 <br><br>
 * 
 * 语法格式：OPEN SHARE TABLE [ALL | 表名1, 表名2, ...] ON [SELECT,INSERT,DELETE,UPDATE] TO [用户名 | SIGN 用户签名 , ...]
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public class OpenShareTableParser extends ShareTableParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:OPEN\\s+SHARE\\s+TABLE)\\s+([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:OPEN\\s+SHARE\\s+TABLE)\\s+([\\w\\W]+?)\\s+(?i)(?:ON)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的开放数据表共享解析器
	 */
	public OpenShareTableParser() {
		super();
	}

	/**
	 * 判断匹配“OPEN SHARE TABLE”命令
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("OPEN SHARE TABLE", input);
		}
		Pattern pattern = Pattern.compile(OpenShareTableParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“OPEN SHARE TABLE”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回OpenShareTable命令
	 */
	public OpenShareTable split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(OpenShareTableParser.REGEX);
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

		// 取出数据表集合和用户签名集合
		String tables = matcher.group(1);
		String operator = matcher.group(2);
		String names = matcher.group(3);

		// 命令
		OpenShareTable cmd = new OpenShareTable();
		// 解析参数
		split(cmd, online, tables, operator, names);
		// 命令原语
		cmd.setPrimitive(input);
		
		return cmd;
	}

}