/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.util.regex.*;

import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;

/**
 * 检测本地实体文件的内容编码。<br><br>
 * 
 * 命令格式：CHECK ENTITY CHARSET 任意多个文件路径 <br><br>
 * 
 * 只在FRONT节点执行。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/16/2019
 * @since laxcus 1.0
 */
public class CheckEntityCharsetParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+ENTITY\\s+CHARSET)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造实例
	 */
	public CheckEntityCharsetParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK ENTITY CHARSET", input);
		}
		Pattern pattern = Pattern.compile(CheckEntityCharsetParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析语句，返回命令
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回CheckEntityCharset命令
	 */
	public CheckEntityCharset split(String input, boolean online) {
		Pattern pattern = Pattern.compile(CheckEntityCharsetParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		// 生成命令
		CheckEntityCharset cmd = new CheckEntityCharset();
		String diskText = matcher.group(1);
		// 解析磁盘文件
		String[] elements = splitCommaSymbol(diskText);
		for(String filename : elements) {
			File file = new File(filename);
			boolean success = (file.exists() && file.isFile());
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, filename);
			}
			// 保存磁盘文件
			cmd.add(file);
		}

		cmd.setPrimitive(input); // 保存原语

		return cmd;
	}	

}
