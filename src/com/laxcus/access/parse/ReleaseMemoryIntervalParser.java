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
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 释放节点内存解析器 <BR><BR>
 * 
 * 格式：SET RELEASE MEMORY INTERVAL XXX [HOUR|MINUTE|SECOND] TO [节点地址|ALL]
 * 
 * @author scott.liang
 * @version 1.0 12/07/2018
 * @since laxcus 1.0
 */
public class ReleaseMemoryIntervalParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+RELEASE\\s+MEMORY\\s+INTERVAL)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的释放节点内存解析器
	 */
	public ReleaseMemoryIntervalParser() {
		super();
	}
	
	/**
	 * 判断匹配“SET RELEASE MEMORY INTERVAL”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET RELEASE MEMORY INTERVAL", input);
		}
		Pattern pattern = Pattern.compile(ReleaseMemoryIntervalParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RELEASE MEMORY”语句
	 * @param input 输入语句
	 * @return 返回ReleaseMemoryInterval命令
	 */
	public ReleaseMemoryInterval split(String input) {
		Pattern pattern = Pattern.compile(ReleaseMemoryIntervalParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReleaseMemoryInterval cmd = new ReleaseMemoryInterval();
		
		// 解析时间
		String time = matcher.group(1);
		long interval = ConfigParser.splitTime(time, -1);
		if (interval == -1) {
			return null;
		}
		cmd.setInterval(interval);

		// 目标地址
		String suffix = matcher.group(2);
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