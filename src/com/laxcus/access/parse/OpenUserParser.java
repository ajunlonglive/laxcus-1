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
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 开放用户账号解析器 <br><br>
 * 
 * 语法格式：<br>
 * 1. OPEN USER 用户名称 <br>
 * 2. OPEN USER SIGN 数字签名 <br>
 * 
 * @author scott.liang
 * @version 1.0 1/4/2020
 * @since laxcus 1.0
 */
public class OpenUserParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:OPEN\\s+USER)\\s+([\\w\\W]+)\\s*$";

	/** 开放用户账号. OPEN USER 用户名称 */
	private final static String OPEN_USER = "^\\s*(?i)(?:OPEN\\s+USER)\\s+([^\\s^\\p{Cntrl}]{1,}?)\\s*$";

	/** 开放用户账号(输入SHA256串码,64个字符)，OPEN USER SIGN 64个16进制字符串 */
	private final static String OPEN_SHA256_USER = "^\\s*(?i)(?:OPEN\\s+USER\\s+SIGN)\\s+([0-9a-fA-F]{64})\\s*$";

	/**
	 * 构造默认的开放用户账号解析器
	 */
	public OpenUserParser() {
		super();
	}

	/**
	 * 检查与“开放用户账号”的语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("OPEN USER", input);
		}
		Pattern pattern = Pattern.compile(OpenUserParser.OPEN_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(OpenUserParser.OPEN_USER);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 开放用户账号<br>
	 * @param input 格式: OPEN USER 用户名
	 * @param online 在线检查
	 * @return 返回OpenUser命令
	 */
	public OpenUser split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		String username = null;
		Siger siger = null;
		
		Pattern pattern = Pattern.compile(OpenUserParser.OPEN_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			username = matcher.group(1);
			// 判断是SHA256的16进制字符串
			if (Siger.validate(username)) {
				siger = new Siger(username);
			}
		} else {
			pattern = Pattern.compile(OpenUserParser.OPEN_USER);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				username = matcher.group(1);
				siger = SHAUser.doUsername(username);
			}
		}
		
		// 如果是空指针时...
		if (siger == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 判断账号存在！
		if (online) {
			if (!hasUser(siger)) {
				throwableNo(FaultTip.NOTFOUND_X, username);
			}
		}

		OpenUser cmd = new OpenUser(siger);
		cmd.setPlainText(username);
		cmd.setPrimitive(input);

		return cmd;
	}
	
}