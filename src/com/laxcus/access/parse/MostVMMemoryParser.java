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
 * 节点虚拟机内存最大使用率解析器 <br>
 * 
 * WATCH节点执行，投递给所在集群的节点。
 * 
 * @author scott.liang
 * @version 1.0 1/21/2020
 * @since laxcus 1.0
 */
public class MostVMMemoryParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+MOST\\s+VM\\s+MEMORY)\\s+(.+?)\\s+(?i)(?:TO)\\s([\\w\\W]+)\\s*$";

	/**
	 * 构造节点虚拟机内存最大使用率解析器
	 */
	public MostVMMemoryParser() {
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
			return isCommand("SET MOST VM MEMORY", input);
		}
		Pattern pattern = Pattern.compile(MostVMMemoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析节点虚拟机内存最大使用率语句。<br>
	 * 
	 * 说明：因为存在“%”符号，会影响到其它节点的打印输出，所以不保存命令原语。
	 * 
	 * @param input 输入语句
	 * @return 返回MostVMMemory命令
	 */
	public MostVMMemory split(String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(MostVMMemoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 虚拟机内存占比
		String rate = matcher.group(1);
		// 如果不是比例值，弹出错误
		if (!ConfigParser.isRate(rate)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		double value = ConfigParser.splitRate(rate, 0.0f);
		if (value <= 0.0f) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, rate);
		}
		MostVMMemory cmd = new MostVMMemory(value); 
		
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