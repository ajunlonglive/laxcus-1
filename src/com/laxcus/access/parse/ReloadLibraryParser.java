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
 * 重新加载各站点的网络安全服务解析器 <BR><BR>
 * 
 * 格式：RELOAD DYNAMIC LIBRARY TO [节点地址|ALL]
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class ReloadLibraryParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:RELOAD\\s+DYNAMIC\\s+LIBRARY\\s+TO)\\s([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的重新加载各站点的网络安全服务解析器
	 */
	public ReloadLibraryParser() {
		super();
	}
	
	/**
	 * 判断匹配“RELOAD DYNAMIC LIBRARY”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RELOAD DYNAMIC LIBRARY", input);
		}
		Pattern pattern = Pattern.compile(ReloadLibraryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RELOAD DYNAMIC LIBRARY”语句
	 * @param input 输入语句
	 * @return 返回ReloadLibrary命令
	 */
	public ReloadLibrary split(String input) {
		Pattern pattern = Pattern.compile(ReloadLibraryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReloadLibrary cmd = new ReloadLibrary();

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
