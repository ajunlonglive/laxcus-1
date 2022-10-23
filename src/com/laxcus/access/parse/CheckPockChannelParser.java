/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.cyber.*;
import com.laxcus.util.tip.*;

/**
 * 检测穿透信道解析器<br><br>
 * 
 * 只在FRONT节点执行。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public final class CheckPockChannelParser extends MultiUserParser {

	/** 检测穿透信道语句 **/
	private final static String REGEX = "^\\s*(?i)(CHECK\\s+POCK\\s+CHANNEL)\\s*$";
	
	/**
	 * 构造检测穿透信道解析器
	 */
	public CheckPockChannelParser() {
		super();
	}

	/**
	 * 判断匹配“CHECK POCK CHANNEL”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(CheckPockChannelParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“CHECK POCK CHANNEL”语句
	 * @param input 输入语句
	 * @return 返回CheckPockChannel命令
	 */
	public CheckPockChannel split(String input) {
		Pattern pattern = Pattern.compile(CheckPockChannelParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		CheckPockChannel cmd = new CheckPockChannel();

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}