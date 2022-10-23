/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.traffic.*;
import com.laxcus.util.tip.*;

/**
 * 并行流量测试解析器。<br>
 * 
 * 语法：PARALLEL MULTI SWARM 发送的数据长度[M] FIXP数据包长度[K] FIXP数据子包长度（字节）子包间隔时间（毫秒） TO 目标节点 持续次数 ITERATE 并行数目
 * 
 * @author scott.liang
 * @version 1.0 10/4/2018
 * @since laxcus 1.0
 */
public class ParallelMultiSwarmParser extends SyntaxParser {

//	private final static String REGEX_TITLE = "^\\s*(?i)(?:PARALLEL\\s+MULTI\\s+SWARM)\\s+(?i)([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PARALLEL\\s+MULTI\\s+SWARM)\\s+(.+?)\\s+(?i)(?:ITERATE)\\s+([1-9][0-9]*)\\s*$";

	/** 局部 **/
	private final static String REGION = "^\\s*(?i)(?:PARALLEL)\\s+(.+?)\\s+(?i)(?:ITERATE)\\s+([1-9][0-9]*)\\s*$";

	/**
	 * 构造默认的并行流量测试解析器
	 */
	public ParallelMultiSwarmParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		// 判断标准匹配
		if (simple) {
			return isCommand("PARALLEL MULTI SWARM", input);
		}

		// 判断标准匹配
		Pattern pattern = Pattern.compile(ParallelMultiSwarmParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

		// 判断子段数据
		pattern = Pattern.compile(ParallelMultiSwarmParser.REGION);
		matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

		// 取出子段参数
		String sub = matcher.group(1);
		MultiSwarmParser parser = new MultiSwarmParser();
		return parser.matches(simple, sub);
	}

	//	/**
	//	 * 判断语句匹配
	//	 * @param input 输入语句
	//	 * @return 返回真或者假
	//	 */
	//	public boolean matches(String input) {
	//		// 判断标准匹配
	//		Pattern pattern = Pattern.compile(ParallelMultiSwarmParser.REGEX);
	//		Matcher matcher = pattern.matcher(input);
	//		if (!matcher.matches()) {
	//			return false;
	//		}
	//
	//		// 判断子段数据
	//		pattern = Pattern.compile(ParallelMultiSwarmParser.REGION);
	//		matcher = pattern.matcher(input);
	//		if (!matcher.matches()) {
	//			return false;
	//		}
	//
	//		// 取出子段参数
	//		String sub = matcher.group(1);
	//		MultiSwarmParser parser = new MultiSwarmParser();
	//		return parser.matches(sub);
	//	}

	/**
	 * 解析并行流量测试命令
	 * @param input 输入语句
	 * @return 返回ParallelMultiSwarm命令
	 */
	public ParallelMultiSwarm split(String input) {
		Pattern pattern = Pattern.compile(ParallelMultiSwarmParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		pattern = Pattern.compile(ParallelMultiSwarmParser.REGION);
		matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String first = matcher.group(1);
		int iterate = Integer.parseInt(matcher.group(2));

		MultiSwarmParser parser = new MultiSwarmParser();
		MultiSwarm sub = parser.split(first);

		// 生成命令退出
		ParallelMultiSwarm cmd = new ParallelMultiSwarm();
		cmd.setIterate(iterate);
		cmd.setMultiSwarm(sub);
		cmd.setPrimitive(input);

		return cmd;
	}

}
