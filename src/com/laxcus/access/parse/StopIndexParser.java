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

import com.laxcus.access.schema.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 卸载索引命令解析器
 * 
 * @author scott.liang
 * @version 1.0 8/24/2012
 * @since laxcus 1.0
 */
public class StopIndexParser extends SyntaxParser {

	/** 卸载索引键 : STOP INDEX|UNLOAD INDEX schema.table [FROM ip_address,...] **/
	private final static String STOP_INDEX1 = "^\\s*(?i)(?:STOP\\s+INDEX|UNLOAD\\s+INDEX)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";
	private final static String STOP_INDEX2 = "^\\s*(?i)(?:STOP\\s+INDEX|UNLOAD\\s+INDEX)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)FROM\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的卸载索引命令解析器
	 */
	public StopIndexParser() {
		super();
	}

	/**
	 * 判断匹配卸载语法："STOP INDEX|UNLOAD INDEX ..."
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			boolean success = isCommand("STOP INDEX", input);
			if (!success) {
				success = isCommand("UNLOAD INDEX", input);
			}
			return success;
		}
		Pattern pattern = Pattern.compile(StopIndexParser.STOP_INDEX2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(StopIndexParser.STOP_INDEX1);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}
	
	/**
	 * 解析卸载语法："STOP INDEX|UNLOAD INDEX ..."
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回StopIndex命令。
	 */
	public StopIndex split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(StopIndexParser.STOP_INDEX2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(StopIndexParser.STOP_INDEX1);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		Space space = new Space(matcher.group(1), matcher.group(2));
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		StopIndex cmd = new StopIndex(space);
		cmd.setPrimitive(input);

		if (matcher.groupCount() > 2) {
			String suffix = matcher.group(3);
			List<Node> list = splitSites(suffix, SiteTag.DATA_SITE);
			cmd.addSites(list);
		}
		
		return cmd;
	}

}