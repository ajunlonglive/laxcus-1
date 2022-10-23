/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.command.cyber.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * FRONT在线用户虚拟空间解析器 <br>
 * 
 * 语法：SET FRONT CYBER 成员数 阀值  TO [ALL|site address, ....]
 * 只限WATCH节点管理员操作。
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class SetFrontCyberParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+FRONT\\s+CYBER)\\s+([0-9]+)\\s+([0-9]+\\s*\\%)\\s+(?i)(?:TO)\\s+(.+?)\\s*$";

	/**
	 * 构造FRONT在线用户虚拟空间解析器
	 */
	public SetFrontCyberParser() {
		super();
	}

	/**
	 * 判断匹配语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET FRONT CYBER", input);
		}
		Pattern pattern = Pattern.compile(SetFrontCyberParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析FRONT在线用户虚拟空间语句
	 * @param input 输入语句
	 * @return 返回SetFrontCyber命令
	 */
	public SetFrontCyber split(String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(SetFrontCyberParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String member = matcher.group(1);
		String threshold = matcher.group(2);
		String suffix = matcher.group(3);
		
		// 成员数
		if (!ConfigParser.isInteger(member)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, member);
		}
		// 阀值
		if (!ConfigParser.isRate(threshold)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, threshold);
		}

		// 定义命令
		SetFrontCyber cmd = new SetFrontCyber();
		cmd.setPersons(ConfigParser.splitInteger(member, 0));
		cmd.setThreshold(ConfigParser.splitRate(threshold, 0.0f));
		// 超过百分比时
		if (cmd.getThreshold() > 100.0f) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, threshold);
		}

		// 全部，指定节点
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			byte[] families = new byte[] { SiteTag.GATE_SITE, SiteTag.CALL_SITE };
			List<Node> nodes = splitSites(suffix, families);
			cmd.addAll(nodes);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}