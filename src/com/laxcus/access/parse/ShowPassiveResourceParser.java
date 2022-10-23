/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.cross.*;
import com.laxcus.util.tip.*;

/**
 * 被授权人显示授权人开放给自己的授权资源<br>
 * FRONT节点发出，作用到GATE节点。 <br><br>
 * 
 * 语法格式：SHOW PASSIVE RESOURCE FROM [ALL | username , SIGN {digitx}, ...]
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class ShowPassiveResourceParser extends MultiUserParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SHOW\\s+PASSIVE\\s+RESOURCE)\\s+([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private static final String REGEX = "^\\s*(?i)(?:SHOW\\s+PASSIVE\\s+RESOURCE\\s+FROM)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 生成显示用户的共享表资源解析器
	 */
	public ShowPassiveResourceParser() {
		super();
	}

	/**
	 * 判断匹配“SHOW PASSIVE TABLE”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SHOW PASSIVE RESOURCE", input);
		}
		Pattern pattern = Pattern.compile(ShowPassiveResourceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SHOW PASSIVE TABLE”语句
	 * @param input 输入语句
	 * @return 返回ShowPassiveResource命令
	 */
	public ShowPassiveResource split(String input) {
		Pattern pattern = Pattern.compile(ShowPassiveResourceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 取出授权人签名字符串
		String authorizers = matcher.group(1);

		// 命令
		ShowPassiveResource cmd = new ShowPassiveResource();
		// 全部授权人或者部分授权人签名
		if (!authorizers.matches("^\\s*(?i)(?:ALL)\\s*$")) {
			splitSigers(authorizers, cmd);
		}
		// 命令原语
		cmd.setPrimitive(input);

		// 返回结果
		return cmd;
	}

}