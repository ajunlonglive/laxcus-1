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
 * 加载数据块命令解析器
 * 
 * 包括：加载、卸载数据块命令、更新数据块尺寸命令、显示数据块尺寸命令、统计数据块尺寸命令
 * 
 * @author scott.liang
 * @version 1.1 8/12/2012
 * @since laxcus 1.0
 */
public class LoadEntityParser extends SyntaxParser {

	/** 从磁盘上加载指定的数据块到内存中，格式: LOAD ENTITY schema.table [TO ip-address,...]**/
	private final static String LOAD_ENTITY1 = "^\\s*(?i)(?:LOAD\\s+ENTITY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";
	private final static String LOAD_ENTITY2 = "^\\s*(?i)(?:LOAD\\s+ENTITY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)TO\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造加载数据块命令解析器
	 */
	public LoadEntityParser() {
		super();
	}
	
	/**
	 * 判断匹配加载数据块语法：“LOAD ENTITY ...”
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("LOAD ENTITY", input);
		}
		Pattern pattern = Pattern.compile(LoadEntityParser.LOAD_ENTITY2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(LoadEntityParser.LOAD_ENTITY1);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析加载数据块语法
	 * @param input 格式: LOAD ENTITY schema.table [TO address, address, ...]
	 * @param online 在线状态
	 * @return 返回LoadEntity命令
	 */
	public LoadEntity split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(LoadEntityParser.LOAD_ENTITY2);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(LoadEntityParser.LOAD_ENTITY1);
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

		LoadEntity cmd = new LoadEntity(space);

		if (matcher.groupCount() > 2) {
			String suffix = matcher.group(3);
			List<Node> list = splitSites(suffix, SiteTag.DATA_SITE);
			cmd.addSites(list);
		}

		cmd.setPrimitive(input);

		return cmd;
	}


}