/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.tip.*;

/**
 * EACH签名解析器 <br>
 * 
 * @author scott.liang
 * @version 1.0 5/23/2018
 * @since laxcus 1.0
 */
public class BuildEachParser extends SyntaxParser {

	/** 正则表达式。字符串 **/
	private final static String REGEX1 = "^\\s*(?i)(?:BUILD\\s+EACH)(?i)(\\s+CASE\\s+|\\s+NOT\\s+CASE\\s+|\\s+)([\\w\\W]+?)\\s+(?i)(?:ENCODE\\s+BY)\\s+(?i)(UTF8|UTF16|UTF32)\\s*$";
	
	/** 文件格式 **/
	private final static String REGEX2 = "^\\s*(?i)(?:BUILD\\s+EACH)\\s+([\\w\\W]+?)\\s*";

	/**
	 * 建立EACH签名解析器
	 */
	public BuildEachParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("BUILD EACH", input);
		}
		
		Pattern pattern = Pattern.compile(BuildEachParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(BuildEachParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析EACH签名语句
	 * @param input 输入语句
	 * @return 返回散列命令
	 */
	public BuildEach split(String input) {
		String ignore = "";
		String charset = "";
		String text = null;
		// 判断匹配，取参数
		Pattern pattern = Pattern.compile(BuildEachParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			ignore = matcher.group(1);
			text = matcher.group(2);
			charset = matcher.group(3);
		} else {
			pattern = Pattern.compile(BuildEachParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			if (match) {
				text = matcher.group(1);
			}
		}
		// 不匹配，弹出错误
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		BuildEach cmd = new BuildEach();
		if (ignore.matches("^\\s*(?i)(CASE)\\s*$")) {
			cmd.setIgnore(false);
		} else if (ignore.matches("^\\s*(?i)(NOT\\s+CASE)\\s*$")) {
			cmd.setIgnore(true);
		} else {
			cmd.setIgnore(true);
		}

		// 设置字符集
		if (charset.matches("^\\s*(?i)(UTF8)\\s*$")) {
			cmd.setFamily(BuildEach.UTF8);
		} else if (charset.matches("^\\s*(?i)(UTF16)\\s*$")) {
			cmd.setFamily(BuildEach.UTF16);
		} else if (charset.matches("^\\s*(?i)(UTF32)\\s*$")) {
			cmd.setFamily(BuildEach.UTF32);
		}

		// 文本
		cmd.setPlant(text);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}