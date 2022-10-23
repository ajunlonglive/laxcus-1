/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 中间缓存尺寸解析器。<br><br>
 * 
 * 语法格式：SET MIDDLE BUFFER 数字 TO 用户名称 | SIGN 数字签名
 * 
 * @author scott.liang
 * @version 1.0 01/10/2021
 * @since laxcus 1.0
 */
public class SetMiddleBufferParser extends MultiUserParameterParser {

	/** 设置中间缓存尺寸正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+MIDDLE\\s+BUFFER)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的中间缓存尺寸解析器
	 */
	public SetMiddleBufferParser() {
		super();
	}

	/**
	 * 判断语句匹配“SET MIDDLE BUFFER ...”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET MIDDLE BUFFER", input);
		}
		Pattern pattern = Pattern.compile(SetMiddleBufferParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析中间缓存尺寸语句
	 * @param input 中间缓存尺寸语句
	 * @return 返回SetMiddleBuffer命令
	 */
	public SetMiddleBuffer split(String input) {
		Pattern pattern = Pattern.compile(SetMiddleBufferParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String number = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String line = matcher.group(2);
		
		// 解析缓存尺寸
		if (!ConfigParser.isLongCapacity(number)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, number);
		}

		// 解析组成尺寸
		long capacity = ConfigParser.splitLongCapacity(number, -1);
		if (capacity < 1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, number);
		}

		// 生成命令
		SetMiddleBuffer cmd = new SetMiddleBuffer(capacity);

		// 解析任意多个账号签名
		splitSigers(line, cmd);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}
