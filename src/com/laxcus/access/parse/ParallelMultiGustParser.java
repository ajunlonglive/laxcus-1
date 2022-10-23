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
 * 语法：PARALLEL MULTI GUST 发送的数据长度[M] FIXP数据包长度[K] FIXP数据子包长度（字节）子包间隔时间（毫秒） FROM 源头节点 TO 目标节点 持续次数 ITERATE 并行数目
 * 
 * @author scott.liang
 * @version 1.0 10/5/2018
 * @since laxcus 1.0
 */
public class ParallelMultiGustParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:PARALLEL\\s+MULTI\\s+GUST)\\s+(?i)([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PARALLEL\\s+MULTI\\s+GUST)\\s+(.+?)\\s+(?i)(?:ITERATE)\\s+([1-9][0-9]*)\\s*$";

	/** 局部 **/
	private final static String REGION = "^\\s*(?i)(?:PARALLEL)\\s+(.+?)\\s+(?i)(?:ITERATE)\\s+([1-9][0-9]*)\\s*$";

	/**
	 * 构造默认的并行流量测试解析器
	 */
	public ParallelMultiGustParser() {
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
			// 判断标准匹配
			return isCommand("PARALLEL MULTI GUST", input);
		}
		
		// 判断标准匹配
		Pattern pattern = Pattern.compile(ParallelMultiGustParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

		// 判断子段数据
		pattern = Pattern.compile(ParallelMultiGustParser.REGION);
		matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return false;
		}

		// 取出子段参数
		String sub = matcher.group(1);
		MultiGustParser parser = new MultiGustParser();
		return parser.matches(simple, sub);
	}

	/**
	 * 解析并行流量测试命令
	 * @param input 输入语句
	 * @return 返回ParallelMultiGust命令
	 */
	public ParallelMultiGust split(String input) {
		Pattern pattern = Pattern.compile(ParallelMultiGustParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		pattern = Pattern.compile(ParallelMultiGustParser.REGION);
		matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String first = matcher.group(1);
		int iterate = Integer.parseInt(matcher.group(2));

		MultiGustParser parser = new MultiGustParser();
		MultiGust sub = parser.split(first);

		// 生成命令退出
		ParallelMultiGust cmd = new ParallelMultiGust();
		cmd.setIterate(iterate);
		cmd.setMultiGust(sub);
		cmd.setPrimitive(input);

		return cmd;
	}

	//	public static void main(String[] args) {
	//		String input = "Parallel Multi Gust 10m 512k 32k 0ms From bank://127.0.0.1:6111_6111, top://127.0.0.1:3000_3000 To home://127.0.0.1:5000_5000, log://127.0.0.1:5188_5188 Iterate 1";
	//		ParallelMultiGustParser e = new ParallelMultiGustParser();
	//		boolean b = e.matches(input);
	//		System.out.printf("result is %s\n", b);
	//		
	//		SyntaxChecker checker = new SyntaxChecker();
	//		b = checker.isParallelMultiGust(input);
	//		System.out.printf("match is %s\n", b);
	//	}

}
