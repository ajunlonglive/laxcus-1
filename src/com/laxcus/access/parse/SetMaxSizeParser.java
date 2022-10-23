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
 * 最大磁盘空间解析器。<br><br>
 * 
 * 语法格式：SET MAX SIZE 数字[M|G|T|P] TO 用户名称 | SIGN 数字签名
 * 
 * @author scott.liang
 * @version 1.0 03/24/2018
 * @since laxcus 1.0
 */
public class SetMaxSizeParser extends MultiUserParameterParser {

	/** 设置最大磁盘空间正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+MAX\\s+SIZE)\\s+(0|[1-9][0-9]+)\\s*(M|G|T|P)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的最大磁盘空间解析器
	 */
	public SetMaxSizeParser() {
		super();
	}

	/**
	 * 判断语句匹配“SET MAX SIZE ...”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET MAX SIZE", input);
		}
		Pattern pattern = Pattern.compile(SetMaxSizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析最大磁盘空间语句
	 * @param input 最大磁盘空间语句
	 * @return 返回SetMaxSize命令
	 */
	public SetMaxSize split(String input) {
		Pattern pattern = Pattern.compile(SetMaxSizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String digit = matcher.group(1);
		String unit = matcher.group(2);
		
		// 数目尺寸
		long value = Long.parseLong(digit);
		if ("M".equalsIgnoreCase(unit)) {
			value = value * Laxkit.MB;
		} else if ("G".equalsIgnoreCase(unit)) {
			value = value * Laxkit.GB;
		} else if ("T".equalsIgnoreCase(unit)) {
			value = value * Laxkit.TB;
		} else if ("P".equalsIgnoreCase(unit)) {
			value = value * Laxkit.PB;
		}
		
		// 以逗号为依据，分割用户签名
		String line = matcher.group(3);

		SetMaxSize cmd = new SetMaxSize(value);

		// 解析任意多个账号签名
		splitSigers(line, cmd);

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}