/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.access.schema.*;
import com.laxcus.command.limit.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.tip.*;

/**
 * 限制操作解析器
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus
 */
class LimitParser extends SyntaxParser {

	/**
	 * 默认的限制操作解析器
	 */
	protected LimitParser() {
		super();
	}

	/**
	 * 解析用户级限制操作语句
	 * @param cmd
	 * @param input
	 */
	protected void splitUser(PostLimit cmd, String input) {
		String[] symbols = splitCommaSymbol(input);
		for(String symbol : symbols) {
			byte operator = LimitOperator.translate(symbol);
			// 判断操作有效，否则弹出异常
			if(!LimitOperator.isOperator(operator)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, symbol);
			}
			UserLimitItem item = new UserLimitItem(operator);
			cmd.add(item);
		}
	}
	
	/**
	 * 解析数据库极限制操作语句
	 * @param cmd
	 * @param tags
	 * @param databases
	 * @param online
	 */
	protected void splitSchemas(PostLimit cmd, String tags, String databases, boolean online) {
		String[] schemas = splitCommaSymbol(databases);
		String[] symbols = splitCommaSymbol(tags);
		for (String str : schemas) {
			Fame fame = new Fame(str);
			// 如果是在线，检索这个数据库有效
			if (online) {
				if(!hasSchema(fame)) {
					throwableNo(FaultTip.NOTFOUND_X, fame);
				}
			}
			for (String symbol : symbols) {
				byte operator = LimitOperator.translate(symbol);
				// 判断操作符有效，否则弹出异常
				if(!LimitOperator.isOperator(operator)) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, symbol);
				}
				SchemaLimitItem item = new SchemaLimitItem(operator, fame);
				cmd.add(item);
			}
		}
	}
	
	/**
	 * 解析表级限制操作语句
	 * @param cmd
	 * @param tags
	 * @param tables
	 * @param online
	 */
	protected void splitTables(PostLimit cmd, String tags, String tables, boolean online) {
		String[] spaces = splitCommaSymbol(tables);
		String[] symbols = splitCommaSymbol(tags);
		for(String str : spaces) {
			Space space = new Space(str);
			// 如果在线，检索这个表名有效
			if(online) {
				if(!hasTable(space)) {
					throwableNo(FaultTip.NOTFOUND_X, space);
				}
			}
			for (String symbol : symbols) {
				byte operator = LimitOperator.translate(symbol);
				if(!LimitOperator.isOperator(operator)) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, symbol);
				}

				TableLimitItem item = new TableLimitItem(operator, space);
				cmd.add(item);
			}
		}
	}

}
