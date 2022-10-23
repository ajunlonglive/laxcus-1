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
 * 数据持有人显示自己共享出来的数据资源<br>
 * FRONT节点发出，作用到GATE节点。 <br><br>
 * 
 * 语法格式：SHOW OPEN RESOURCE TO [ALL | username , SIGN {digitx}, ...]
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class ShowOpenResourceParser extends MultiUserParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SHOW\\s+OPEN\\s+RESOURCE)\\s+([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private static final String REGEX = "^\\s*(?i)(?:SHOW\\s+OPEN\\s+RESOURCE\\s+TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 生成显示用户的共享表资源解析器
	 */
	public ShowOpenResourceParser() {
		super();
	}

	/**
	 * 判断匹配“SHOW OPEN RESOURCE”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SHOW OPEN RESOURCE", input);
		}
		Pattern pattern = Pattern.compile(ShowOpenResourceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SHOW OPEN RESOURCE”语句
	 * @param input 输入语句
	 * @return 返回ShowOpenResource命令
	 */
	public ShowOpenResource split(String input) {
		Pattern pattern = Pattern.compile(ShowOpenResourceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 取出被授权人签名字符串
		String conferrers = matcher.group(1);

		// 命令
		ShowOpenResource cmd = new ShowOpenResource();
		// 解析参数
		if (!conferrers.matches("^\\s*(?i)(?:ALL)\\s*$")) {
			splitSigers(conferrers, cmd);
		}
		// 命令原语
		cmd.setPrimitive(input);

		// 返回结果
		return cmd;
	}

}