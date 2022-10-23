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
import com.laxcus.util.tip.*;

/**
 * 停止边缘计算服务解析器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2019
 * @since laxcus 1.0
 */
public class StopTubServiceParser extends SyntaxParser {

	/** 语法格式 **/
	private final static String REGEX = "^\\s*(?i)(?:STOP\\s+TUB\\s+SERVICE)\\s+([0-9]+?)(\\s+[\\w\\W]+\\s*|\\s*)$";

	/**
	 * 构造默认的扫描数据块命令解析器
	 */
	public StopTubServiceParser() {
		super();
	}

	/**
	 * 检查匹配扫描数据块语法：“STOP TUB SERVICE”
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("STOP TUB SERVICE", input);
		}
		Pattern pattern = Pattern.compile(StopTubServiceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析“STOP TUB SERVICE”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回StopTubService命令
	 */
	public StopTubService split(String input, boolean online) {
		Pattern pattern = Pattern.compile(StopTubServiceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 进程ID
		long processId = Long.parseLong(matcher.group(1));
		// 解析参数
		String suffix = matcher.group(2);

		//		String[] args = null;
		//		if (suffix != null && suffix.trim().length() > 0) {
		//			args = splitSpaceSymbol(suffix);
		//		}

		StopTubService cmd = new StopTubService(processId, suffix);
		cmd.setPrimitive(input);
		return cmd;
	}
}