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
import com.laxcus.util.tip.*;

/**
 * 建立限制操作解析器。<br><br>
 * 
 * 限制操作是在发生数据“写操作”失败后、因为可能存在数据一致性错误，用户出于业务需要（如各种交易、生产等关键领域），需要将数据锁定，等到修复后再使用。<br>
 * 限制操作有两个操作符：READ、WRITE。<br>
 * “READ” 表示指定的用户、数据库、表的“读操作”被锁定，不能进行相关操作。<br>
 * “WRITE”表示指定的用户、数据库、表的“写操作”被锁定，不能进行相关操作。<br><br>
 * 
 * 读操作有“SELECT”，写操作包括：“INSERT、DELETE、UPDATE”。<br>
 * 这个命令被用户使用，通过FRONT站点进行操作。<br>
 * 
 * 语法：
 * <1> CREATE LIMIT READ,WRITE ON USER
 * <2> CREATE LIMIT READ,WRITE ON DATABASE 数据库名, ...
 * <3> CREATE LIMIT READ,WRITE ON TABLE 数据库名.表名, ...
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus
 */
public class CreateLimitParser extends LimitParser {

	/** 用户限制操作，语法： CREATE LIMIT [READ,WRITE] ON USER **/
	private final static String CREATE_USER_LIMIT = "^\\s*(?i)(?:CREATE\\s+LIMIT)\\s+([\\w\\W]+?)\\s+(?i)(?:ON\\s+USER)\\s*$";

	/** 数据库限制操作，语法：CREATE LIMIT [READ,WRITE] ON DATABASE [数据库名1, ...] **/
	private final static String CREATE_SCHEMA_LIMIT = "^\\s*(?i)(?:CREATE\\s+LIMIT)\\s+([\\w\\W]+?)\\s+(?i)(?:ON\\s+DATABASE)\\s+([\\w\\W]+?)\\s*";

	/** 数据表限制操作，语法：CREATE LIMIT [READ,WRITE] ON TABLE [表名1, ....] **/
	private final static String CREATE_TABLE_LIMIT = "^\\s*(?i)(?:CREATE\\s+LIMIT)\\s+([\\w\\W]+?)\\s+(?i)(?:ON\\s+TABLE)\\s+([\\w\\W]+?)\\s*";

	/**
	 * 构造默认的建立限制操作解析器
	 */
	public CreateLimitParser() {
		super();
	}

	/**
	 * 判断匹配建立限制操作语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CREATE LIMIT", input);
		}
		Pattern pattern = Pattern.compile(CreateLimitParser.CREATE_USER_LIMIT);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(CreateLimitParser.CREATE_SCHEMA_LIMIT);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			pattern = Pattern.compile(CreateLimitParser.CREATE_TABLE_LIMIT);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}


	/**
	 * 解析语法
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回CreateLimit命令。
	 */
	public CreateLimit split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		CreateLimit cmd = new CreateLimit();
		cmd.setPrimitive(input);
		
		Pattern pattern = Pattern.compile(CreateLimitParser.CREATE_USER_LIMIT);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if(match) {
			String tags = matcher.group(1);
			splitUser(cmd, tags);
		}

		if(!match) {
			pattern = Pattern.compile(CreateLimitParser.CREATE_SCHEMA_LIMIT);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if(match) {
				String tags = matcher.group(1);
				String databases = matcher.group(2);
				splitSchemas(cmd, tags, databases, online);
			}
		}

		if(!match) {
			pattern = Pattern.compile(CreateLimitParser.CREATE_TABLE_LIMIT);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if(match) {
				String tags = matcher.group(1);
				String tables = matcher.group(2);
				splitTables(cmd, tags, tables, online);
			}
		}

		// 空集合
		if(cmd.isEmpty()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		return cmd;
	}

}