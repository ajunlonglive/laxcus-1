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
 * 修改用户账号密码解析器 <br><br>
 * 
 * 语法格式：<br>
 * 2. ALTER USER 用户名 PASSWORD ['XXX']  <br>
 * 3. ALTER USER 用户签名 PASSWORD ['XXX'] <br>
 * 
 * @author scott.liang
 * @version 1.0 5/7/2009
 * @since laxcus 1.0
 */
public class AlterUserParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:ALTER\\s+USER)\\s+([\\w\\W]+)\\s*$";

	/**  修改用户账号密码 **/
	private final static String ALTER_SHA256_USER = "^\\s*(?i)(?:ALTER\\s+USER\\s+SIGN)\\s+([0-9a-fA-F]{64})\\s+PASSWORD\\s+\\'([^\\s^\\p{Cntrl}]+)\\'\\s*$";
	private final static String ALTER_USER = "^\\s*(?i)(?:ALTER\\s+USER)\\s+([\\w\\W]+?)\\s+PASSWORD\\s+\\'([^\\s^\\p{Cntrl}]+)\\'\\s*$";

	/**
	 * 构造默认的修改用户账号密码解析器
	 */
	public AlterUserParser() {
		super();
	}

	/**
	 * 检查与修改账号语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("ALTER USER", input);
		}
		Pattern pattern = Pattern.compile(AlterUserParser.ALTER_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if(!success) {
			pattern = Pattern.compile(AlterUserParser.ALTER_USER);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 修改用户账号密码<br>
	 * @param input 格式: ALTER USER username [IDENTIFIED BY 'xxx'|PASSWORD 'xxx'|PASSWORD='xxx']
	 * @param online 在线检查
	 * @return 返回AlterUser实例
	 */
	public AlterUser split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		AlterUser cmd = null;

		Pattern pattern = Pattern.compile(AlterUserParser.ALTER_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String username = matcher.group(1);
			String password = matcher.group(2);
			Siger siger = SHAUser.doSiger(username);
			// 如果账号不存在弹出异常
			if (online) {
				if (!hasUser(siger)) {
					throwableNo(FaultTip.NOTFOUND_X, username);
				}
			}

			User user = new User(siger, SHAUser.doPassword(password));
			cmd = new AlterUser(user);
			cmd.setPlainText(username);
		} else {
			pattern = Pattern.compile(AlterUserParser.ALTER_USER);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String username = matcher.group(1);
				String password = matcher.group(2);
				// 如果账号不存在弹出异常
				if (online) {
					if (!hasUser(username)) {
						throwableNo(FaultTip.NOTFOUND_X, username);
					}
				}

				User user = new User(username, password);
				cmd = new AlterUser(user);
				cmd.setPlainText(username);
			}
		}

		if (cmd == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		return cmd;
	}

	//	/**
	//	 * 修改用户账号密码<br>
	//	 * @param input 格式: ALTER USER username [IDENTIFIED BY 'xxx'|PASSWORD 'xxx'|PASSWORD='xxx']
	//	 * @param online 在线检查
	//	 * @return 返回AlterUser实例
	//	 */
	//	public AlterUser split(String input, boolean online) {
	//		// 检查是在线状态
	//		if (online) {
	//			checkOnline();
	//		}
	//		
	//		Pattern pattern = Pattern.compile(AlterUserParser.ALTER_USER);
	//		Matcher matcher = pattern.matcher(input);
	//		if (!matcher.matches()) {
	//			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
	//		}
	//
	//		String username = matcher.group(1);
	//		String password = matcher.group(2);
	//
	//		// 如果账号不存在弹出异常
	//		if (online) {
	//			if (!hasUser(username)) {
	//				throwableNo(FaultTip.NOTFOUND_X, username);
	//			}
	//		}
	//
	//		User user = new User(username, password);
	//		AlterUser cmd = new AlterUser(user);
	//		cmd.setPlainText(username);
	//		return cmd;
	//	}

}