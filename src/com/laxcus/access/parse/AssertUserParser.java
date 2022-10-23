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
 * 判断用户名称解析器 <br><br>
 * 
 * 语法格式：<br>
 * ASSERT USER 用户名  <br>
 * 
 * @author scott.liang
 * @version 1.0 5/7/2009
 * @since laxcus 1.0
 */
public class AssertUserParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:ASSERT\\s+USER)\\s+([\\w\\W]+)\\s*$";

	/** 判断用户名称  */
	private final static String ASSERT_SHA256_USER = "^\\s*(?i)(?:ASSERT\\s+USER\\s+SIGN)\\s+([0-9a-fA-F]{64})\\s*$";
	private final static String ASSERT_USER = "^\\s*(?i)(?:ASSERT\\s+USER)\\s+([^\\s]{1,})\\s*$";

	/**
	 * 构造默认的判断用户名称解析器
	 */
	public AssertUserParser() {
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
			return isCommand("ASSERT USER", input);
		}
		Pattern pattern = Pattern.compile(AssertUserParser.ASSERT_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(AssertUserParser.ASSERT_USER);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 判断用户名称<br>
	 * @param input 格式: ASSERT USER username 
	 * @return 返回AssertUser实例
	 */
	public AssertUser split(String input) {
		Pattern pattern = Pattern.compile(AssertUserParser.ASSERT_SHA256_USER);
		Matcher matcher = pattern.matcher(input);
		AssertUser cmd = null;
		
		if (matcher.matches()) {
			String username = matcher.group(1);
			Siger siger = SHAUser.doSiger(username);
			// 用户名称
			cmd = new AssertUser(siger);
			cmd.setPrimitive(input);
			return cmd;
		} else {
			pattern = Pattern.compile(AssertUserParser.ASSERT_USER);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String username = matcher.group(1);
				// 用户名称
				cmd = new AssertUser(username);
				cmd.setPrimitive(input);
			}
		}

		if (cmd == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		return cmd;
	}

}