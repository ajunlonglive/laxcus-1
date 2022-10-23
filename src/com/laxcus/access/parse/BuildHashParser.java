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
 * 散列算法解析器 <br>
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class BuildHashParser extends SyntaxParser {
	
	private final static String REGEX_TITLE = "^\\s*(?i)(?:BUILD)\\s+(?i)(?:SHA1|SHA256|SHA512|MD5)(\\s*|\\s+[\\w\\W]*)$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:BUILD)\\s+(?i)(SHA1|SHA256|SHA512|MD5)(?i)(\\s+CASE\\s+|\\s+NOT\\s+CASE\\s+|\\s+)([\\w\\W]+?)\\s*$";

	/**
	 * 建立散列算法解析器
	 */
	public BuildHashParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			Pattern pattern = Pattern.compile(BuildHashParser.REGEX_TITLE);
			Matcher matcher = pattern.matcher(input);
			return matcher.matches();
		}
		Pattern pattern = Pattern.compile(BuildHashParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析散列算法语句
	 * @param input 输入语句
	 * @return 返回散列命令
	 */
	public BuildHash split(String input) {
		// 判断匹配，取参数
		Pattern pattern = Pattern.compile(BuildHashParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String family = matcher.group(1);
		String ignore = matcher.group(2);
		String text = matcher.group(3);

		BuildHash cmd = new BuildHash();
		if (family.matches("^\\s*(?i)(SHA1)\\s*$")) {
			cmd.setFamily(BuildHash.SHA1);
		} else if (family.matches("^\\s*(?i)(SHA256)\\s*$")) {
			cmd.setFamily(BuildHash.SHA256);
		} else if (family.matches("^\\s*(?i)(SHA512)\\s*$")) {
			cmd.setFamily(BuildHash.SHA512);
		} else if (family.matches("^\\s*(?i)(MD5)\\s*$")) {
			cmd.setFamily(BuildHash.MD5);
		}

		if(ignore.matches("^\\s*(?i)(CASE)\\s*$")) {
			cmd.setIgnore(false);
		} else if(ignore.matches("^\\s*(?i)(NOT\\s+CASE)\\s*$")) {
			cmd.setIgnore(true);
		} else {
			cmd.setIgnore(true);
		}

		// 文本
		cmd.setPlant(text);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}