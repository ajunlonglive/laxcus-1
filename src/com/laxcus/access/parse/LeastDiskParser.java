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
import com.laxcus.util.disk.*;
import com.laxcus.util.tip.*;

/**
 * 节点最小磁盘空间限制解析器 <br>
 * 
 * WATCH节点执行，投递给所在集群的节点。
 * 
 * @author scott.liang
 * @version 1.0 8/8/2019
 * @since laxcus 1.0
 */
public class LeastDiskParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+LEAST\\s+DISK)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s([\\w\\W]+)\\s*$";

	/** 无限制 **/
	private final static String UNLIMIT = "^\\s*(?i)(UNLIMIT)\\s*$";
	
	/** 参数格式：路径 磁盘最小容量 、路径  磁盘最小容量占比（百分数） **/
	private final static String PATH_VALUE = "^\\s*([\\w\\W]+)\\s+([0-9]+[\\.0-9]*\\s*[\\w\\W]+)\\s*$";
	
	/**
	 * 构造节点最小磁盘空间限制解析器
	 */
	public LeastDiskParser() {
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
			return isCommand("SET LEAST DISK", input);
		}
		Pattern pattern = Pattern.compile(LeastDiskParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析“路径 参数”格式值
	 * @param input 语句
	 * @return 返回类实例，出错弹出异常
	 */
	private LeastPath splitSubPath( String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(LeastDiskParser.PATH_VALUE);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 路径和关联参数
		String path = matcher.group(1);
		String suffix = matcher.group(2);

		// 三种参数：1. 整型容量  2. 浮点容量  3. 比例值
		if (ConfigParser.isLongCapacity(suffix)) {
			long value = ConfigParser.splitLongCapacity(suffix, -1);
			if (value == -1) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}
			// 路径 磁盘最小容量（整数）
			return new LeastPath(path, value);
		} else if (ConfigParser.isDoubleCapacity(suffix)) {
			double value = ConfigParser.splitDoubleCapacity(suffix, 0.0f);
			if (value == 0.0f) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}
			// 路径  磁盘最小容量（浮点数）
			return new LeastPath(path, new Double(value).longValue());
		} else if (ConfigParser.isRate(suffix)) {
			double rate = ConfigParser.splitRate(suffix, 0.0f);
			if (rate == 0.0f) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}
			// 设备 磁盘空间比例
			return new LeastPath(path, rate);
		}
		// 异常
		throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);

		return null;
	}

	/**
	 * 解析设备路径
	 * @param cmd 命令
	 * @param input 输入语句
	 */
	private void splitPath(LeastDisk cmd, String input) {
		String[] items = splitCommaSymbol(input);
		for (String item : items) {
			LeastPath path = splitSubPath(item);
			if (path != null) {
				cmd.addPath(path);
			}
		}
	}

	/**
	 * 解析节点最小磁盘空间限制语句。<br>
	 * 
	 * 说明：因为存在“%”符号，会影响到其它节点的打印输出，所以不保存命令原语。
	 * 
	 * @param input 输入语句
	 * @return 返回LeastDisk命令
	 */
	public LeastDisk split(String input) {
		LeastDisk cmd = new LeastDisk();
		
		// 解析判断
		Pattern pattern = Pattern.compile(LeastDiskParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 磁盘空间数
		String prefix = matcher.group(1);

		// 前四种：无限制、以“G/M/K”为后缀的字符串（浮点数）、比例值
		// 前四种都不是，是路径/容量、路径/磁盘最小占比
		if (prefix.matches(LeastDiskParser.UNLIMIT)) {
			cmd.setUnlimit();
		} else if (ConfigParser.isLongCapacity(prefix)) {
			long value = ConfigParser.splitLongCapacity(prefix, -1);
			if (value == -1) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
			}
			cmd.setCapacity(value);
		} else if (ConfigParser.isDoubleCapacity(prefix)) {
			double value = ConfigParser.splitDoubleCapacity(prefix, 0.0f);
			if (value <= 0.0f) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
			}
			cmd.setCapacity(new Double(value).longValue());
		} else if (ConfigParser.isRate(prefix)) {
			double rate = ConfigParser.splitRate(prefix, 0.0f);
			if (rate <= 0.0f) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
			}
			cmd.setRate(rate);
		} else {
			splitPath(cmd, prefix);
		}
		
		// 目标地址
		String suffix = matcher.group(2);
		// 判断是“LOCAL”、“ALL”关键字，或者其它节点地址
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addSites(nodes);
		}

		return cmd;
	}

}