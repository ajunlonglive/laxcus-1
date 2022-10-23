/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.site.entrance.*;
import com.laxcus.util.tip.*;

/**
 * 设置定位GATE站点模式解析器 <br><br>
 * 
 * 语法格式：<br>
 * SET SHADOW MODE [HASH|ASSERT]  <br>
 * 
 * @author scott.liang
 * @version 1.0 7/18/2019
 * @since laxcus 1.0
 */
public class ShadowModeParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+SHADOW\\s+MODE)\\s+(?i)(HASH|ASSERT)\\s*$";

	/**
	 * 构造默认的设置定位GATE站点模式解析器
	 */
	public ShadowModeParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET SHADOW MODE", input);
		}
		Pattern pattern = Pattern.compile(ShadowModeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析设置定位GATE站点模式<br>
	 * @param input 格式: SET SHADOW MODE [HASH|ASSERT]
	 * @return 返回ShadowMode实例
	 */
	public ShadowMode split(String input) {
		Pattern pattern = Pattern.compile(ShadowModeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 判断是HASH模式
		String suffix = matcher.group(1);
		boolean hash = suffix.matches("^\\s*(?i)(HASH)\\s*$");
	
		// 生成命令
		ShadowMode cmd = new ShadowMode(hash);
		cmd.setPrimitive(input);
		return cmd;
	}

}