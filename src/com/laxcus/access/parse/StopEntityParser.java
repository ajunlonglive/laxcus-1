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
 * 卸载数据块命令解析器
 * 
 * @author scott.liang
 * @version 1.0 8/12/2012
 * @since laxcus 1.0
 */
public class StopEntityParser extends SyntaxParser {

	/** 从内存中卸载指定的数据块内容，格式: STOP ENTITY|UNLOAD ENTITY schema.table [FROM host_address,...] **/
	private final static String STOP_ENTITY1 = "^\\s*(?i)(?:STOP\\s+ENTITY|UNLOAD\\s+ENTITY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";
	private final static String STOP_ENTITY2 = "^\\s*(?i)(?:STOP\\s+ENTITY|UNLOAD\\s+ENTITY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)FROM\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的卸载数据块命令解析器
	 */
	public StopEntityParser() {
		super();
	}

	/**
	 * 判断匹配卸载数据块语法：“STOP ENTITY...”
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			boolean b = isCommand("STOP ENTITY", input);
			if (!b) {
				b = isCommand("UNLOAD ENTITY", input);
			}
			return b;
		}
		Pattern pattern = Pattern.compile(StopEntityParser.STOP_ENTITY2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(StopEntityParser.STOP_ENTITY1);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析卸载数据块
	 * @param input STOP ENTITY schema.table [FROM address, address, ...]
	 * @param online 在线状态
	 * @return 返回StopEntity命令
	 */
	public StopEntity split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(StopEntityParser.STOP_ENTITY2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(StopEntityParser.STOP_ENTITY1);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		Space space = new Space(matcher.group(1), matcher.group(2));
		// 在线检查数据表名
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		StopEntity cmd = new StopEntity(space);
		cmd.setPrimitive(input);

		if (matcher.groupCount() > 2) {
			String suffix = matcher.group(3);
			List<Node> list = splitSites(suffix, SiteTag.DATA_SITE);
			cmd.addSites(list);
		}
		return cmd;
	}

}
