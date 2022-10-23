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

import com.laxcus.command.reload.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 扫描堆栈命令解析器 <BR><BR>
 * 
 * 两个命令：<BR>
 * OPEN COMMAND STACK 间隔时间 TO [节点地址|ALL] <BR>
 * CLOSE COMMAND STACK FROM [节点地址|ALL] <BR><BR>
 * 
 * @author scott.liang
 * @version 1.0 11/23/2015
 * @since laxcus 1.0
 */
public class ScanCommandStackParser extends SyntaxParser {

	/** 启动正则表达式 **/
	private final static String OPEN_REGEX = "^\\s*(?i)(?:OPEN\\s+COMMAND\\s+STACK)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+(.+)\\s*$";

	/** 关闭正则表达式 **/
	private final static String CLOSE_REGEX = "^\\s*(?i)(?:CLOSE\\s+COMMAND\\s+STACK\\s+FROM)\\s+(.+?)\\s*$";

	/**
	 * 构造扫描堆栈命令解析器
	 */
	public ScanCommandStackParser() {
		super();
	}

	/**
	 * 判断匹配扫描堆栈命令语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			boolean b = isCommand("OPEN COMMAND STACK", input);
			if (!b) {
				b = isCommand("CLOSE COMMAND STACK", input);
			}
			return b;
		}
		Pattern pattern = Pattern.compile(ScanCommandStackParser.OPEN_REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(ScanCommandStackParser.CLOSE_REGEX);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}
	
	/**
	 * 解析扫描堆栈命令语句
	 * @param input 输入语句
	 * @return 返回ScanCommandStack命令
	 */
	public ScanCommandStack split(String input) {
		ScanCommandStack cmd = splitOpen(input);
		if (cmd == null) {
			cmd = splitClose(input);
		}
		if (cmd == null) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		cmd.setPrimitive(input);
		return cmd;
	}
	
	/**
	 * 启动扫描命令堆栈
	 * @param input 输入语句
	 * @return 命令实例
	 */
	private ScanCommandStack splitOpen(String input) {
		// 无限制时间
		Pattern pattern = Pattern.compile(ScanCommandStackParser.OPEN_REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		ScanCommandStack cmd = new ScanCommandStack();
		cmd.setStart(true);
		
		// 解析时间
		long interval = ConfigParser.splitTime(matcher.group(1), -1);
		if (interval < 1) {
			return null;
		}
		cmd.setInterval(interval);

		// 站点
		String suffix = matcher.group(2);
		if (!suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
			List<Node> nodes = splitSites(suffix);
			cmd.addSites(nodes);
		}

		return cmd;
	}

	/**
	 * 停止扫描命令堆栈
	 * @param input 输入语句
	 * @return 命令实例
	 */
	private ScanCommandStack splitClose(String input) {
		// 无限制时间
		Pattern pattern = Pattern.compile(ScanCommandStackParser.CLOSE_REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}

		ScanCommandStack cmd = new ScanCommandStack();
		cmd.setStart(false);

		String suffix = matcher.group(1);
		if (!suffix.matches("^\\s*(?i)(ALL)\\s*$")) {
			List<Node> nodes = splitSites(suffix);
			cmd.addSites(nodes);
		}
		return cmd;
	}
}