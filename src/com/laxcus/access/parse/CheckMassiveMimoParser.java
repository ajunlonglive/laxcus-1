/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.net.*;
import java.util.regex.*;

import com.laxcus.command.cyber.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 检测多入多出命令解析器 <br>
 * 
 * 用户使用，从FRONT节点发往GATE/ENTRANCE/CALL节点。
 * 
 * @author scott.liang
 * @version 1.0 2/21/2022
 * @since laxcus 1.0
 */
public class CheckMassiveMimoParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+MASSIVE\\s+MIMO\\s+TO)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造检测多入多出命令解析器
	 */
	public CheckMassiveMimoParser() {
		super();
	}

	/**
	 * 判断匹配被WATCH监视节点的定时刷新语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK MASSIVE MIMO", input);
		}
		Pattern pattern = Pattern.compile(CheckMassiveMimoParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析检测MASSIVE MIMO命令
	 * @param input 输入语句
	 * @return 返回CheckMassiveMimo命令
	 */
	public CheckMassiveMimo split(String input) {
		Pattern pattern = Pattern.compile(CheckMassiveMimoParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String syntax = matcher.group(1);

		CheckMassiveMimo cmd = new CheckMassiveMimo();
		// 保存命令原语
		cmd.setPrimitive(input);

		// 判断显示自己的命令
		if (syntax.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			return cmd;
		}

		// 判断语法正确
		if (!Node.validate(syntax)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, syntax);
		}
		// 取出节点地址
		try {
			Node node = new Node(syntax);
			cmd.setSite(node);
		} catch (UnknownHostException e) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, syntax);
		}
		
		// 必须是网关节点
		if (!SiteTag.isGateway(cmd.getSite().getFamily())) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, syntax);
		}

		return cmd;
	}

}