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
import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;

/**
 * 获得数据表在集群的分布图谱解析器。<br><br>
 * 
 * 语法格式：PRINT ENTITY STUBS DIAGRAM 数据库.表 <br>
 * 
 * @author scott.liang
 * @version 1.0 11/11/2020
 * @since laxcus 1.0
 */
public class PrintStubsDiagramParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PRINT\\s+ENTITY\\s+STUBS\\s+DIAGRAM)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的获得数据表在集群的分布图谱解析器
	 */
	public PrintStubsDiagramParser() {
		super();
	}

	/**
	 * 判断获得数据表在集群的分布图谱语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("PRINT ENTITY STUBS DIAGRAM", input);
		}
		Pattern pattern = Pattern.compile(PrintStubsDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析获得数据表在集群的分布图谱语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回PrintStubsDiagram命令
	 */
	public PrintStubsDiagram split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(PrintStubsDiagramParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		String text = matcher.group(1);
		// 判断表名有效
		if (!Space.validate(text)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, text);
		}

		// 生成数据表名
		Space space = new Space(text);
		// 在线检查表
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, text);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		PrintStubsDiagram cmd = new PrintStubsDiagram(space);
		cmd.setPrimitive(input); // 保存原语
		return cmd;
	}

}