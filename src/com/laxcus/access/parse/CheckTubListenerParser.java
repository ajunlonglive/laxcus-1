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
 * 检测边缘服务监听解析器。<br><br>
 * 
 * 语法格式：<br>
 * CHECK TUB LISTENER  <br>
 * 
 * @author scott.liang
 * @version 1.0 10/17/2020
 * @since laxcus 1.0
 */
public class CheckTubListenerParser extends SyntaxParser {

	/** 检测边缘服务监听命令集合 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+TUB\\s+LISTENER)\\s*$";

	/**
	 * 构造检测边缘服务监听解析器
	 */
	public CheckTubListenerParser() {
		super();
	}

	/**
	 * 判断语句匹配“CHECK TUB LISTENER”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(CheckTubListenerParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“CHECK TUB LISTENER ...”命令
	 * @param input 输入语句
	 * @return 返回CheckTubListener命令
	 */
	public CheckTubListener split(String input) {
		// 判断正确
		Pattern pattern = Pattern.compile(CheckTubListenerParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		CheckTubListener cmd = new CheckTubListener();
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}