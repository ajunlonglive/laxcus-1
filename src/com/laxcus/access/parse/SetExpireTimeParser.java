/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.util.*;
import com.laxcus.command.access.user.*;
import com.laxcus.util.tip.*;

/**
 * 用户账号到期时间解析器。<br><br>
 * 
 * 语法格式：SET EXPIRE TIME 数字 TO 用户名称 | SIGN 数字签名
 * 
 * @author scott.liang
 * @version 1.0 01/04/2020
 * @since laxcus 1.0
 */
public class SetExpireTimeParser extends MultiUserParameterParser {

	/** 设置用户账号到期时间正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+EXPIRE\\s+TIME)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的用户账号到期时间解析器
	 */
	public SetExpireTimeParser() {
		super();
	}

	/**
	 * 判断语句匹配“SET EXPIRE TIME ...”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET EXPIRE TIME", input);
		}
		Pattern pattern = Pattern.compile(SetExpireTimeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析用户账号到期时间语句
	 * @param input 用户账号到期时间语句
	 * @return 返回SetExpireTime命令
	 */
	public SetExpireTime split(String input) {
		Pattern pattern = Pattern.compile(SetExpireTimeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String time = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String suffix = matcher.group(2);

		// 转换为时间戳格式
		long timestamp = 0;
		// 不是无限制时间
		if (!time.matches("^\\s*(?i)(UNLIMIT)\\s*$")) {
			timestamp = CalendarGenerator.splitTimestamp(time);
		}
		SetExpireTime cmd = new SetExpireTime(timestamp);

		// 解析任意多个账号签名
		splitSigers(suffix, cmd);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}