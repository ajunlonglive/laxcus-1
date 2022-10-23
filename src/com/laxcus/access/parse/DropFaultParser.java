/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.limit.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.tip.*;

/**
 * 撤销故障锁定解析器。<br><br>
 * 
 * 语法：<br>
 * DROP FAULT ON USER <br>
 * DROP FAULT ON DATABASE 数据库名 <br>
 * DROP FAULT ON TABLE 数据表名 <br>
 * 
 * @author scott.liang
 * @version 1.0 3/26/2017
 * @since laxcus 1.0
 */
public class DropFaultParser extends DataLockParser {

	/** 用户级锁定，语法： DROP FAULT ON USER **/
	private final static String DROP_USER_LOCK = "^\\s*(?i)(?:DROP\\s+FAULT)\\s+(?i)(?:ON\\s+USER)\\s*$";

	/** 数据库级锁定，语法：DROP FAULT ON DATABASE [数据库名1, ...] **/
	private final static String DROP_SCHEMA_LOCK = "^\\s*(?i)(?:DROP\\s+FAULT)\\s+(?i)(?:ON\\s+DATABASE)\\s+([\\w\\W]+?)\\s*";

	/** 数据表级锁定，语法：DROP FAULT ON TABLE [表名1, ....] **/
	private final static String DROP_TABLE_LOCK = "^\\s*(?i)(?:DROP\\s+FAULT)\\s+(?i)(?:ON\\s+TABLE)\\s+([\\w\\W]+?)\\s*";

	/**
	 * 构造默认的撤销故障锁定解析器
	 */
	public DropFaultParser() {
		super();
	}

	/**
	 * 判断语法正确
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DROP FAULT", input);
		}
		Pattern pattern = Pattern.compile(DropFaultParser.DROP_USER_LOCK);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(DropFaultParser.DROP_SCHEMA_LOCK);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(DropFaultParser.DROP_TABLE_LOCK);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析撤销故障锁定命令
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回DropFault命令。
	 */
	public DropFault split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		DropFault cmd = new DropFault();
		cmd.setPrimitive(input);
		
		Pattern pattern = Pattern.compile(DropFaultParser.DROP_USER_LOCK);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			UserFaultItem item = new UserFaultItem();
			cmd.add(item);
		}

		if (!match) {
			pattern = Pattern.compile(DropFaultParser.DROP_SCHEMA_LOCK);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				String databases = matcher.group(1);
				splitSchemas(cmd, databases, online);
			}
		}

		if (!match) {
			pattern = Pattern.compile(DropFaultParser.DROP_TABLE_LOCK);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				String tables = matcher.group(1);
				splitTables(cmd, tables, online);
			}
		}

		// 空集合
		if(cmd.isEmpty()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		return cmd;
	}

}