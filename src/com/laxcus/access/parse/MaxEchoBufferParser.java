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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 设置节点的最大异步缓存尺寸解析器 <br>
 * 
 * 语法：SET ECHO BUFFER {digit}{K|M|G}  TO [LOCAL|ALL|site address, ....]
 * FRONT节点只能设置自己的异步缓存，WATCH节点可以设置自己和集群的异步缓存。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2019
 * @since laxcus 1.0
 */
public class MaxEchoBufferParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+ECHO\\s+BUFFER)\\s+(?i)([\\w\\W]+)\\s+(?i)(?:TO)\\s+(.+?)\\s*$";

	/**
	 * 构造设置节点的最大异步缓存尺寸解析器
	 */
	public MaxEchoBufferParser() {
		super();
	}

	/**
	 * 判断匹配语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET ECHO BUFFER", input);
		}
		Pattern pattern = Pattern.compile(MaxEchoBufferParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析设置节点的最大异步缓存尺寸语句
	 * @param input 输入语句
	 * @return 返回MaxEchoBuffer命令
	 */
	public MaxEchoBuffer split(String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(MaxEchoBufferParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String prefix = matcher.group(1);
		String suffix = matcher.group(2);
		
		// 判断内存参数
		if (!ConfigParser.isLongCapacity(prefix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
		}

		// 解析缓存尺寸
		long capacity = ConfigParser.splitLongCapacity(prefix, 0);
		// 定义命令
		MaxEchoBuffer cmd = new MaxEchoBuffer(capacity);

		// 本地，全部，指定节点
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}