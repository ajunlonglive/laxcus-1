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
 * 提交故障锁定解析器。<br><br>
 * 
 * 语法格式：<br>
 * <1> CREATE FAULT ON USER <br>
 * <2> CREATE FAULT ON DATABAE 数据库名, ... <br>
 * <3> CREATE FAULT ON TABLE 数据表名, ... <br><br>
 * 
 * @author scott.liang
 * @version 1.0 3/26/2017
 * @since laxcus 1.0
 */
public class CreateFaultParser extends DataLockParser {

	/** 用户级锁定，语法： CREATE FAULT ON USER **/
	private final static String CREATE_USER_LOCK = "^\\s*(?i)(?:CREATE\\s+FAULT)\\s+(?i)(?:ON\\s+USER)\\s*$";

	/** 数据库级锁定，语法：CREATE FAULT ON DATABASE [数据库名1, ...] **/
	private final static String CREATE_SCHEMA_LOCK = "^\\s*(?i)(?:CREATE\\s+FAULT)\\s+(?i)(?:ON\\s+DATABASE)\\s+([\\w\\W]+?)\\s*";

	/** 数据表级锁定，语法：CREATE FAULT ON TABLE [表名1, ....] **/
	private final static String CREATE_TABLE_LOCK = "^\\s*(?i)(?:CREATE\\s+FAULT)\\s+(?i)(?:ON\\s+TABLE)\\s+([\\w\\W]+?)\\s*";

	/**
	 * 构造默认的提交故障锁定解析器
	 */
	public CreateFaultParser() {
		super();
	}

	/**
	 * 判断匹配提交故障锁定语句
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CREATE FAULT", input);
		}
		Pattern pattern = Pattern.compile(CreateFaultParser.CREATE_USER_LOCK);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(CreateFaultParser.CREATE_SCHEMA_LOCK);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(CreateFaultParser.CREATE_TABLE_LOCK);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析故障锁定语法
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回CreateFault命令。
	 */
	public CreateFault split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		CreateFault cmd = new CreateFault();
		cmd.setPrimitive(input);

		Pattern pattern = Pattern.compile(CreateFaultParser.CREATE_USER_LOCK);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			UserFaultItem item = new UserFaultItem();
			cmd.add(item);
		}

		if (!match) {
			pattern = Pattern.compile(CreateFaultParser.CREATE_SCHEMA_LOCK);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				String databases = matcher.group(1);
				splitSchemas(cmd, databases, online);
			}
		}

		if (!match) {
			pattern = Pattern.compile(CreateFaultParser.CREATE_TABLE_LOCK);
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