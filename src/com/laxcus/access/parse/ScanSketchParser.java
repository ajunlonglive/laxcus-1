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
 * 分布表数据容量解析器。<br><br>
 * 
 * 语句格式：SCAN SKETCH 数据库名.表名
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public class ScanSketchParser extends SyntaxParser {

	/** 正则表达式 **/
	private static final String REGEX = "^\\s*(?i)(?:SCAN\\s+SKETCH)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]{1,20})\\s*$";

	/**
	 * 构造检测表分布数据容量解析器
	 */
	public ScanSketchParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SCAN SKETCH", input);
		}
		Pattern pattern = Pattern.compile(ScanSketchParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回ScanSketch命令
	 */
	public ScanSketch split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ScanSketchParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String schema = matcher.group(1);
		String table = matcher.group(2);
		Space space = new Space(schema, table);

		// 如果是在线模式，检查表存在
		if (online) {
			boolean success = hasTable(space);
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		ScanSketch cmd = new ScanSketch(space);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}