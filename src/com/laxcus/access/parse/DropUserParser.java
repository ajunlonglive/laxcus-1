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
 * 删除用户账号解析器 <br><br>
 * 
 * 语法格式：<br>
 * 1. DROP USER 用户名称 <br>
 * 2. DROP USER SIGN 数字签名 <br>
 * 
 * @author scott.liang
 * @version 1.0 5/7/2009
 * @since laxcus 1.0
 */
public class DropUserParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:DROP\\s+USER)\\s+([\\w\\W]+)\\s*$";

	/** 删除用户账号. DROP USER 用户名称 */
	private final static String DROP_USER = "^\\s*(?i)(?:DROP\\s+USER)\\s+([^\\s^\\p{Cntrl}]{1,}?)\\s*$";

	/** 删除用户账号(输入SHA256串码,64个字符)，DROP USER SIGN 64个16进制字符串 */
	private final static String DROP_SHA256_USER = "^\\s*(?i)(?:DROP\\s+USER\\s+SIGN)\\s+([0-9a-fA-F]{64})\\s*$";

	/**
	 * 构造默认的删除用户账号解析器
	 */
	public DropUserParser() {
		super();
	}
	
	/**
	 * 检查与“删除用户账号”的语句匹配
	 * @param input 输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DROP USER", input);
		}
		
		Pattern pattern = Pattern.compile(DropUserParser.DROP_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(DropUserParser.DROP_USER);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 删除用户账号<br>
	 * @param input 格式: DROP USER 用户名
	 * @param online 在线检查
	 * @return 返回DropUser命令
	 */
	public DropUser split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		String username = null;
		Siger siger = null;
		
		Pattern pattern = Pattern.compile(DropUserParser.DROP_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			username = matcher.group(1);
			// 判断是SHA256的16进制字符串
			if (Siger.validate(username)) {
				siger = new Siger(username);
			}
		} else {
			pattern = Pattern.compile(DropUserParser.DROP_USER);
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

		DropUser cmd = new DropUser(siger);
		cmd.setPlainText(username);
		cmd.setPrimitive(input);

		return cmd;
	}

//	/**
//	 * 检查与“删除用户账号”的语句匹配
//	 * @param input 输入语句
//	 * @return 如果是返回“真”，否则“假”。
//	 */
//	public boolean isDropUser(String input) {
//		Pattern pattern = Pattern.compile(DropUserParser.DROP_USER);
//		Matcher matcher = pattern.matcher(input);
//		return matcher.matches();
//	}
//
//	/**
//	 * 删除用户账号<br>
//	 * @param input 格式: DROP USER 用户名
//	 * @param online 在线检查
//	 * @return 返回DropUser命令
//	 */
//	public DropUser splitDropUser(String input, boolean online) {
//		Pattern pattern = Pattern.compile(DropUserParser.DROP_USER);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//		String username = matcher.group(1);
//
//		// 账号不存在
//		if (online) {
//			if (!hasUser(username)) {
//				throwable(FaultTip.NOTFOUND_X, username);
//			}
//		}
//
//		Siger siger = SHAUser.doUsername(username);
//		DropUser cmd = new DropUser(siger);
//		cmd.setPlainText(username);
////		cmd.setPrimitive(input);
//
//		return cmd;
//	}
//
//	/**
//	 * 检查与“删除用户账号（SHA256码）”的语句匹配
//	 * @param input 输入语句
//	 * @return 如果是返回“真”，否则“假”。
//	 */
//	public boolean isDropSHA256User(String input) {
//		Pattern pattern = Pattern.compile(DropUserParser.DROP_SHA256_USER);
//		Matcher matcher = pattern.matcher(input);
//		return matcher.matches();
//	}
//
//	/**
//	 * 删除用户账号 (SHA256，必须是64个16进制字符) <br>
//	 * @param input 格式: DROP USER SIGN {digit}[64]
//	 * @return 返回DropUser命令
//	 */
//	public DropUser splitDropSHA256User(String input, boolean online) {
//		Pattern pattern = Pattern.compile(DropUserParser.DROP_SHA256_USER);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throwable(FaultTip.INCORRECT_SYNTAX_X, input);
//		}
//
//		// 16进制字符串
//		String hex = matcher.group(1);
//		Siger siger = new Siger(hex);
//
//		// 账号不存在
//		if (online) {
//			if (!hasUser(siger)) {
//				throwable(FaultTip.NOTFOUND_X, hex);
//			}
//		}
//
//		DropUser cmd = new DropUser(siger);
////		cmd.setPrimitive(input);
//		return cmd;
//	}

}
