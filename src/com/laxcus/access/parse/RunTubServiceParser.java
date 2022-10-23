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
 * 启动边缘计算服务解析器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2019
 * @since laxcus 1.0
 */
public class RunTubServiceParser extends SyntaxParser {

	/** 语法格式 **/
	private final static String REGEX = "^\\s*(?i)(?:RUN\\s+TUB\\s+SERVICE)\\s+([\\w\\W]+?)(\\s+[\\w\\W]+\\s*|\\s*)$";

	/**
	 * 构造默认的扫描数据块命令解析器
	 */
	public RunTubServiceParser() {
		super();
	}

	/**
	 * 检查匹配扫描数据块语法：“RUN TUB SERVICE”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RUN TUB SERVICE", input);
		}
		Pattern pattern = Pattern.compile(RunTubServiceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RUN TUB SERVICE”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回RunTubService命令
	 */
	public RunTubService split(final String input, boolean online) {
		Pattern pattern = Pattern.compile(RunTubServiceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Naming naming = new Naming(matcher.group(1));
		if (online) {
			if (!hasTubTag(naming)) {
				throwableNo(FaultTip.NOTFOUND_X, naming);
			}
		}

		// 解析参数
		String suffix = matcher.group(2);
		
//		String[] args = null;
//		if (suffix != null && suffix.trim().length() > 0) {
//			args = splitSpaceSymbol(suffix);
//		}

		RunTubService cmd = new RunTubService(naming, suffix);
		cmd.setPrimitive(input);
		return cmd;
	}

	//	public static void main(String[] args) {
	//		String line = "RUN TUB SERVICE RADIO localhost 8999";
	//		RunTubServiceParser e = new RunTubServiceParser();
	//		RunTubService cmd = e.split(line, false);
	//		System.out.println(cmd.getPrimitive());
	//	}
}