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
import com.laxcus.command.scan.*;
import com.laxcus.util.tip.*;

/**
 * 扫描数据块命令解析器
 * 
 * 语法格式：SCAN ENTITY 数据库名.表名。
 * 用于FRONT站点。
 * 
 * @author scott.liang
 * @version 1.0 8/12/2012
 * @since laxcus 1.0
 */
public class ScanEntityParser extends SyntaxParser {
	
	/** 命令前缀 **/
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SCAN\\s+ENTITY)\\s+([\\w\\W]+?)\\s*$";

	/** 语法格式 **/
	private final static String REGEX = "^\\s*(?i)(?:SCAN\\s+ENTITY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/**
	 * 构造默认的扫描数据块命令解析器
	 */
	public ScanEntityParser() {
		super();
	}

	/**
	 * 检查匹配扫描数据块语法：“SCAN ENTITY”
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SCAN ENTITY", input);
		}
		Pattern pattern = Pattern.compile(ScanEntityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析“SCAN ENTITY”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回ScanEntity命令
	 */
	public ScanEntity split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ScanEntityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Space space = new Space(matcher.group(1), matcher.group(2));
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		ScanEntity cmd = new ScanEntity(space);
		cmd.setPrimitive(input);
		return cmd;
	}
}
