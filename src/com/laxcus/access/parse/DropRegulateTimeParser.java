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
import com.laxcus.command.rebuild.*;
import com.laxcus.util.tip.*;

/**
 * 撤销数据优化时间解析器。<br>
 * 此命令在FRONT站点产生，通过Gate站点转发，提交给TOP站点。
 * 
 * 语法格式：<br>
 * DROP REGULATE TIME 数据库名.表名 <br>
 * 
 * @author scott.liang
 * @version 1.2 09/07/2013
 * @since laxcus 1.0
 */
public class DropRegulateTimeParser extends SyntaxParser {

	/** 撤销数据优化语句格式 */
	private final static String REGEX = "^\\s*(?i)(?:DROP\\s+REGULATE\\s+TIME)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/**
	 * 构造撤销数据优化时间解析器
	 */
	public DropRegulateTimeParser() {
		super();
	}

	/**
	 * 判断是撤销数据优化命令
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DROP REGULATE TIME", input);
		}
		Pattern pattern = Pattern.compile(DropRegulateTimeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析撤销数据优化命令
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回DropRegulateTime命令
	 */
	public DropRegulateTime split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(DropRegulateTimeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		// 取数据表名称
		String schema = matcher.group(1);
		String table = matcher.group(2);
		Space space = new Space(schema, table);

		// 判断表存在
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
		}

		DropRegulateTime cmd = new DropRegulateTime(space);
		cmd.setPrimitive(input);
		return cmd;
	}

	//	public static void main(String[] args) {
	//		String input = "DROP REgulate time 多媒体数据库.字体库Lib ";
	//		DropRegulateTimeParser e = new DropRegulateTimeParser();
	//		boolean match = e.matches(input);
	//		System.out.printf("%s IS %s\n", input, match);
	//		DropRegulateTime cmd = e.split(input, false);
	//		System.out.println(cmd);
	//	}
}