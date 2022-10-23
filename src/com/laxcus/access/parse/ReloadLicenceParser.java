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

import com.laxcus.command.licence.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 重新加载节点许可证解析器 <BR><BR>
 * 
 * 格式：RELOAD LICENCE TO [节点地址|ALL]
 * 
 * @author scott.liang
 * @version 1.0 7/20/2020
 * @since laxcus 1.0
 */
public class ReloadLicenceParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:RELOAD\\s+LICENCE\\s+TO)\\s([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的重新加载节点许可证解析器
	 */
	public ReloadLicenceParser() {
		super();
	}
	
	/**
	 * 判断匹配“RELOAD LICENCE”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RELOAD LICENCE", input);
		}
		Pattern pattern = Pattern.compile(ReloadLicenceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RELOAD LICENCE”语句
	 * @param input 输入语句
	 * @return 返回ReloadLicence命令
	 */
	public ReloadLicence split(String input) {
		Pattern pattern = Pattern.compile(ReloadLicenceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReloadLicence cmd = new ReloadLicence();

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
