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

import com.laxcus.command.reload.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 重新加载和发布自定义包解析器 <BR><BR>
 * 
 * 格式： RELOAD CUSTOM PACKAGE TO [ 节点地址 ..., ]
 * 
 * @author scott.liang
 * @version 1.0 11/23/2015
 * @since laxcus 1.0
 */
public class ReloadCustomParser extends SyntaxParser {
	
	private final static String REGEX = "^\\s*(?i)(?:RELOAD\\s+CUSTOM\\s+PACKAGE\\s+TO)\\s([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的重新加载和发布自定义包解析器
	 */
	public ReloadCustomParser() {
		super();
	}
	
	/**
	 * 判断匹配“RELOAD CUSTOM PACKAGE”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RELOAD CUSTOM PACKAGE", input);
		}
		Pattern pattern = Pattern.compile(ReloadCustomParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RELOAD CUSTOM PACKAGE”语句
	 * @param input 输入语句
	 * @return 返回ReloadCustom命令
	 */
	public ReloadCustom split(String input) {
		Pattern pattern = Pattern.compile(ReloadCustomParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReloadCustom cmd = new ReloadCustom();

		String suffix = matcher.group(1);
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}