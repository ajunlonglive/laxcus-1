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

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 开放Tigger操作解析器 <br>
 * 
 * WATCH节点执行，投递给所在集群的节点。
 * 
 * @author scott.liang
 * @version 1.0 1/24/2020
 * @since laxcus 1.0
 */
public class OpenTiggerParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:OPEN\\s+TIGGER)\\s+(.+?)\\s+(?i)(?:TO)\\s([\\w\\W]+)\\s*$";

	/**
	 * 构造开放Tigger操作解析器
	 */
	public OpenTiggerParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("OPEN TIGGER", input);
		}
		Pattern pattern = Pattern.compile(OpenTiggerParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析开放Tigger操作语句。<br>
	 * 
	 * @param input 输入语句
	 * @return 返回OpenTigger命令
	 */
	public OpenTigger split(String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(OpenTiggerParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 开放的TIGGER操作类型
		String prefix = matcher.group(1);
		String[] subs = splitCommaSymbol(prefix);

		// 检测类型
		int value = 0;
		if (prefix.matches("^\\s*(?i)(ALL)\\s*$")) {
			value = TigType.ALL;
		} else {
			for (String sub : subs) {
				int type = TigType.translate(sub);
				if (type < 0) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, sub);
				}
				value |= type;
			}
		}

		// 生成
		OpenTigger cmd = new OpenTigger(value); 
		
		// 目标地址
		String suffix = matcher.group(2);
		// 判断是“LOCAL”、“ALL”关键字，或者其它节点地址
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		return cmd;
	}

}