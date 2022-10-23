/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;
import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 检测服务器系统信息解析器 <BR><BR>
 * 
 * 格式：CHECK SYSTEM INFO TO [节点地址|ALL|LOCAL]
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class CheckSystemInfoParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+SYSTEM\\s+INFO)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的检测服务器系统信息解析器
	 */
	public CheckSystemInfoParser() {
		super();
	}
	
	/**
	 * 判断匹配“SET REPLY PACKET SIZE”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK SYSTEM INFO", input);
		}
		Pattern pattern = Pattern.compile(CheckSystemInfoParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“CHECK SYSTEM INFO”语句
	 * @param input 输入语句
	 * @return 返回SetCheckSystemInfo命令
	 */
	public CheckSystemInfo split(String input) {
		Pattern pattern = Pattern.compile(CheckSystemInfoParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		CheckSystemInfo cmd = new CheckSystemInfo();

		// 目标地址
		String suffix = matcher.group(1);
		// 参数
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}