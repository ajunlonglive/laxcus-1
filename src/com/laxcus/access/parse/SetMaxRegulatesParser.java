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
import com.laxcus.util.tip.*;

/**
 * 最大优化表数目解析器。<br><br>
 * 
 * 语法格式：SET MAX REGULATES 数字 TO 用户名称 | SIGN 数字签名
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetMaxRegulatesParser extends MultiUserParameterParser {

	/** 设置最大优化表数目正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+MAX\\s+REGULATES)\\s+([1-9][0-9]{0,4})\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的最大优化表数目解析器
	 */
	public SetMaxRegulatesParser() {
		super();
	}

	/**
	 * 判断语句匹配“SET MAX REGULATES ...”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET MAX REGULATES", input);
		}
		Pattern pattern = Pattern.compile(SetMaxRegulatesParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析最大优化表数目语句
	 * @param input 最大优化表数目语句
	 * @return 返回SetMaxRegulates命令
	 */
	public SetMaxRegulates split(String input) {
		Pattern pattern = Pattern.compile(SetMaxRegulatesParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String number = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String line = matcher.group(2);

		SetMaxRegulates cmd = new SetMaxRegulates(Integer.parseInt(number));

		// 解析任意多个账号签名
		splitSigers(line, cmd);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}
