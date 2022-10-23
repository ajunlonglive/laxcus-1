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
 * 释放节点内存解析器 <BR><BR>
 * 
 * 格式：RELEASE MEMORY TO [节点地址|ALL]
 * 
 * @author scott.liang
 * @version 1.0 10/11/2018
 * @since laxcus 1.0
 */
public class ReleaseMemoryParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:RELEASE\\s+MEMORY\\s+TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的释放节点内存解析器
	 */
	public ReleaseMemoryParser() {
		super();
	}
	
	/**
	 * 判断匹配“RELEASE MEMORY”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RELEASE MEMORY", input);
		}
		Pattern pattern = Pattern.compile(ReleaseMemoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RELEASE MEMORY”语句
	 * @param input 输入语句
	 * @return 返回ReleaseMemory命令
	 */
	public ReleaseMemory split(String input) {
		Pattern pattern = Pattern.compile(ReleaseMemoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReleaseMemory cmd = new ReleaseMemory();

		String suffix = matcher.group(1);
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