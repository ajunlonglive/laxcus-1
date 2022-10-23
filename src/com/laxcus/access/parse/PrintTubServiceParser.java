/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.tub.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 打印边缘计算服务解析器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2019
 * @since laxcus 1.0
 */
public class PrintTubServiceParser extends SyntaxParser {

	/** 语法格式 **/
	private final static String REGEX = "^\\s*(?i)(?:PRINT\\s+TUB\\s+SERVICE)(\\s+[\\w\\W]+\\s*|\\s*)$";

	/**
	 * 构造默认的扫描数据块命令解析器
	 */
	public PrintTubServiceParser() {
		super();
	}

	/**
	 * 检查匹配扫描数据块语法：“PRINT TUB SERVICE”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("PRINT TUB SERVICE", input);
		}
		Pattern pattern = Pattern.compile(PrintTubServiceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析“PRINT TUB SERVICE”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回PrintTubService命令
	 */
	public PrintTubService split(String input, boolean online) {
		Pattern pattern = Pattern.compile(PrintTubServiceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
	
		// 解析参数
		String suffix = matcher.group(1);
		Naming[] namings = null;
		if (suffix != null && suffix.trim().length() > 0) {
			String[] items = splitSpaceSymbol(suffix);
			// 判断成员数目
			int size = (items != null ? items.length : 0);
			if (size > 0) {
				namings = new Naming[size];
				for (int i = 0; i < items.length; i++) {
					namings[i] = new Naming(items[i]);
				}
			}
		}

		PrintTubService cmd = new PrintTubService(namings);
		cmd.setPrimitive(input);
		return cmd;
	}
}