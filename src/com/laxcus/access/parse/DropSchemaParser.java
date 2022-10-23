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
import com.laxcus.util.tip.*;

/**
 * 删除数据库命令解析器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2009
 * @since laxcus 1.0
 */
public class DropSchemaParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:DROP\\s+DATABASE)\\s+([\\w\\W]+)\\s*$";

	/** 删除数据库|显示数据库的语法格式 **/
	private final static String DROP_SCHEMA = "^\\s*(?i)(?:DROP\\s+DATABASE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)\\s*$";

	/**
	 * 构造默认的删除数据库命令解析器
	 */
	public DropSchemaParser() {
		super();
	}

	/**
	 * 判断匹配“删除数据库”语句。带“DROP DATABASE”前缀。
	 * @param input　输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DROP DATABASE", input);
		}
		Pattern pattern = Pattern.compile(DropSchemaParser.DROP_SCHEMA);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 检查删除数据库语句。语句前缀：”DROP DATABASE“。
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回DropSchema命令
	 */
	public DropSchema split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(DropSchemaParser.DROP_SCHEMA);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String name = matcher.group(1);
		if (name.matches("^\\s*(?i)(?:ALL)\\s*$")) {
			// 如果是全部...
		} else if (online) {
			// 在线检查，不存在弹出错误
			Fame fame = new Fame(name);
			if (!hasSchema(fame)) {
				throwableNo(FaultTip.NOTFOUND_X, fame);
			}
		}
		Fame fame = new Fame(name);
		DropSchema cmd = new DropSchema(fame);
		cmd.setPrimitive(input);
		return cmd;
	}

}