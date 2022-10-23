/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.site.front.*;
import com.laxcus.util.tip.*;

/**
 * 检索FRONT站点分布解析器
 * 
 * 语法：
 * SEEK FRONT USER SIGN 签名
 * SEEK FRONT USER 明文
 * 
 * @author scott.liang
 * @version 12/17/2017
 * @since laxcus 1.0
 */
public class SeekFrontUserParser extends SeekUserResourceParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SEEK\\s+FRONT\\s+USER)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的检索FRONT站点分布解析器
	 */
	public SeekFrontUserParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SEEK FRONT USER", input);
		}
		Pattern pattern = Pattern.compile(SeekFrontUserParser.REGEX);
		Matcher matcher = pattern.matcher(input);		
		return matcher.matches();
	}

	/**
	 * 解析检索用户分布站点命令
	 * @param input 输入语句
	 * @return 输出命令
	 */
	public SeekFrontUser split(String input) {
		Pattern pattern = Pattern.compile(SeekFrontUserParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String prefix = matcher.group(1);
		
		SeekFrontUser cmd = new SeekFrontUser();
		// 解析命令
		splitUser(prefix, cmd);

		// 设置原语
		cmd.setPrimitive(input);
		return cmd;
	}

}