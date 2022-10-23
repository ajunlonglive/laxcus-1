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
 * 节点最小内存限制解析器 <br>
 * 
 * WATCH节点执行，投递给所在集群的节点。
 * 
 * @author scott.liang
 * @version 1.0 8/8/2019
 * @since laxcus 1.0
 */
public class LeastMemoryParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+LEAST\\s+MEMORY)\\s+(.+?)\\s+(?i)(?:TO)\\s([\\w\\W]+)\\s*$";

	/** 无限制 **/
	private final static String UNLIMIT = "^\\s*(?i)(UNLIMIT)\\s*$";
	
	/**
	 * 构造节点最小内存限制解析器
	 */
	public LeastMemoryParser() {
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
			return isCommand("SET LEAST MEMORY", input);
		}
		Pattern pattern = Pattern.compile(LeastMemoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析节点最小内存限制语句。<br>
	 * 
	 * 说明：因为存在“%”符号，会影响到其它节点的打印输出，所以不保存命令原语。
	 * 
	 * @param input 输入语句
	 * @return 返回LeastMemory命令
	 */
	public LeastMemory split(String input) {
		LeastMemory cmd = new LeastMemory();
		
		// 解析判断
		Pattern pattern = Pattern.compile(LeastMemoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 内存数
		String memory = matcher.group(1);
		
		// 四种：无限制、以“G/M/K”为后缀的字符串（浮点数）、比例值
		if (memory.matches(LeastMemoryParser.UNLIMIT)) {
			cmd.setUnlimit();
		} else if (ConfigParser.isLongCapacity(memory)) {
			long value = ConfigParser.splitLongCapacity(memory, -1);
			if (value == -1) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, memory);
			}
			cmd.setCapacity(value);
		} else if (ConfigParser.isDoubleCapacity(memory)) {
			double rate = ConfigParser.splitDoubleCapacity(memory, 0.0f);
			if (rate <= 0.0f) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, memory);
			}
			cmd.setCapacity(new Double(rate).longValue());
		} else if (ConfigParser.isRate(memory)) {
			double rate = ConfigParser.splitRate(memory, 0.0f);
			if (rate <= 0.0f) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, memory);
			}
			cmd.setRate(rate);
		} else {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

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