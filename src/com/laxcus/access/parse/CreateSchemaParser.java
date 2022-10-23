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
import com.laxcus.command.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 建立数据库命令解析器
 * 
 * @author scott.liang
 * @version 1.1 12/1/2011
 * @since laxcus 1.0
 */
public class CreateSchemaParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:CREATE\\s+DATABASE)\\s+([\\w\\W]+)\\s*$";

	/** 建立数据库语法格式: CREATE DATABASE [数据库名] MAXSIZE=xxx[M|G|T|P] **/
	private final static String CREATE_SCHEMA = "^\\s*(?i)(?:CREATE\\s+DATABASE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)(\\s+[\\w\\W]+|\\s*)$";
	private final static String MAXSIZE = "^\\s*(?i)(?:MAXSIZE\\s*=\\s*)([0-9]{1,})(?i)(M|G|T|P)\\s*$";

	/**
	 * 构造建立数据库命令解析器
	 */
	public CreateSchemaParser() {
		super();
	}

	/**
	 * 判断匹配“建立数据库”语句
	 * @param input　输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CREATE DATABASE", input);
		}
		Pattern pattern = Pattern.compile(CreateSchemaParser.CREATE_SCHEMA);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“建立数据库”语句。语句前缀：“CREATE DATABASE”
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回数据库对象实例
	 */
	public CreateSchema split(final String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(CreateSchemaParser.CREATE_SCHEMA);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 数据库名称
		String name = matcher.group(1);
		// "ALL"是关键字，不允许使用"ALL"建立数据库名称
		if (name.matches("^\\s*(?i)(?:ALL)\\s*$")) {
			throwableNo(FaultTip.FORBID_KEYWORD_X, name);
		}
		// 如果要求在线模式执行
		else if (online) {
			Fame fame = new Fame(name);
			if (hasSchema(fame)) {
				throwableNo(FaultTip.EXISTED_X, fame);
			}
		}

		// 取出数据库名称
		Schema schema = new Schema(name);
		// 其实参数（可能有或者没有)
		String suffix = matcher.group(2);
		// 解析参数
		if (suffix.trim().length() > 0) {
			pattern = Pattern.compile(CreateSchemaParser.MAXSIZE);
			matcher = pattern.matcher(suffix);
			if (matcher.matches()) {
				String digit = matcher.group(1);
				String unit = matcher.group(2);

				long value = Long.parseLong(digit);
				if ("M".equalsIgnoreCase(unit)) {
					schema.setMaxSize(value * Laxkit.MB);
				} else if ("G".equalsIgnoreCase(unit)) {
					schema.setMaxSize(value * Laxkit.GB);
				} else if ("T".equalsIgnoreCase(unit)) {
					schema.setMaxSize(value * Laxkit.TB);
				} else if ("P".equalsIgnoreCase(unit)) {
					schema.setMaxSize(value * Laxkit.PB);
				}
			} else {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, suffix);
			}
		}
		
		CreateSchema cmd = new CreateSchema(schema);
		cmd.setPrimitive(input);
		return cmd;
	}

}