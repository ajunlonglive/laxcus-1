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
 * 最大并行任务数解析器。<br><br>
 * 
 * 语法格式：SET MAX JOBS 数字 TO 用户名称 | SIGN 数字签名
 * 
 * @author scott.liang
 * @version 1.0 05/07/2017
 * @since laxcus 1.0
 */
public class SetMaxJobsParser extends MultiUserParameterParser {

	/** 设置最大并行任务数正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+MAX\\s+JOBS)\\s+([1-9][0-9]{0,4})\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的最大并行任务数解析器
	 */
	public SetMaxJobsParser() {
		super();
	}

	/**
	 * 判断语句匹配“SET MAX JOBS ...”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET MAX JOBS", input);
		}
		Pattern pattern = Pattern.compile(SetMaxJobsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析最大并行任务数语句
	 * @param input 最大并行任务数语句
	 * @return 返回SetMaxJobs命令
	 */
	public SetMaxJobs split(String input) {
		Pattern pattern = Pattern.compile(SetMaxJobsParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String number = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String line = matcher.group(2);

		SetMaxJobs cmd = new SetMaxJobs(Integer.parseInt(number));

		// 解析任意多个账号签名
		splitSigers(line, cmd);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}
