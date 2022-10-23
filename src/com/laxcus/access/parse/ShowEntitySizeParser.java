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
import com.laxcus.command.access.fast.*;
import com.laxcus.util.tip.*;

/**
 * 显示数据块尺寸命令解析器
 * 
 * 语法格式：SHOW ENTITY SIZE 数据库名.表名
 * 
 * @author scott.liang
 * @version 1.0 8/12/2012
 * @since laxcus 1.0
 */
public class ShowEntitySizeParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SHOW\\s+ENTITY\\s+SIZE)\\s+([\\w\\W]+?)\\s*$";

	/** 显示数据块的尺寸 **/
	private final static String REGEX = "^\\s*(?i)(?:SHOW\\s+ENTITY\\s+SIZE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/**
	 * 构造默认的显示数据块尺寸命令解析器
	 */
	public ShowEntitySizeParser() {
		super();
	}

	/**
	 * 检查匹配显示数据块语法：“SHOW ENTITY SIZE”
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SHOW ENTITY SIZE", input);
		}
		Pattern pattern = Pattern.compile(ShowEntitySizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析"SHOW ENTITY SIZE"语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回ShowEntitySize命令
	 */
	public ShowEntitySize split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ShowEntitySizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Space space = new Space(matcher.group(1), matcher.group(2));
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
		}

		ShowEntitySize cmd = new ShowEntitySize(space);
		cmd.setPrimitive(input);
		return cmd;
	}

	//	public static void main(String[] args) {
	//		String input = "PRINT Entity size 天DATABASE.人TABLE";
	//		PrintEntitySizeParser e = new PrintEntitySizeParser();
	//		PrintEntitySize cmd = e.split(input, false);
	//		System.out.println(cmd.getSpace());
	//	}
}
