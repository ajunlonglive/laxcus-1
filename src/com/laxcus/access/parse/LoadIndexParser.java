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
 * 加载索引命令解析器。
 * 
 * 语法格式：LOAD INDEX 数据库名.表名 [TO 节点地址1, 节点地址2, ...]
 * 
 * @author scott.liang
 * @version 1.0 8/24/2012
 * @since laxcus 1.0
 */
public class LoadIndexParser extends SyntaxParser {

	/** 加载索引键: LOAD INDEX schema.table [TO ip_address, ...] **/
	private final static String LOAD_INDEX1 = "^\\s*(?i)(?:LOAD\\s+INDEX)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";
	private final static String LOAD_INDEX2 = "^\\s*(?i)(?:LOAD\\s+INDEX)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)TO\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的加载索引命令解析器
	 */
	public LoadIndexParser() {
		super();
	}

	/**
	 * 判断匹配加载索引语法："LOAD INDEX ..."
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("LOAD INDEX", input);
		}
		Pattern pattern = Pattern.compile(LoadIndexParser.LOAD_INDEX2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(LoadIndexParser.LOAD_INDEX1);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析加载索引语法： "LOAD INDEX ..."
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回"LoadIndex"命令
	 */
	public LoadIndex split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(LoadIndexParser.LOAD_INDEX2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(LoadIndexParser.LOAD_INDEX1);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		if (!match) {
			throwable(FaultTip.INCORRECT_SYNTAX); // 无效语法
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

		LoadIndex cmd = new LoadIndex(space);
		cmd.setPrimitive(input);

		if (matcher.groupCount() > 2) {
			String suffix = matcher.group(3);
			List<Node> list = splitSites(suffix, SiteTag.DATA_SITE);
			cmd.addSites(list);
		}
		return cmd;
	}

}