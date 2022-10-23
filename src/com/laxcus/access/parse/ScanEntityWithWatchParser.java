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
import com.laxcus.command.scan.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 扫描数据块命令解析器。<br>
 * 
 * 语法格式：SCAN ENTITY 数据库名.表名 TO [ALL | DATA SITE, ....]
 * 用于WATCH站点。
 * 
 * @author scott.liang
 * @version 1.0 8/12/2012
 * @since laxcus 1.0
 */
public class ScanEntityWithWatchParser extends SyntaxParser {

	/** SCAN ENTITY 语句格式 **/
	private static final String REGEX1 = "^\\s*(?i)(?:SCAN\\s+ENTITY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	/** 语法格式 **/
	private final static String REGEX2 = "^\\s*(?i)(?:SCAN\\s+ENTITY)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*$";

	/**
	 * 构造默认的扫描数据块命令解析器
	 */
	public ScanEntityWithWatchParser() {
		super();
	}

	/**
	 * 检查匹配扫描数据块语法：“SCAN ENTITY”
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		// 判断
		if (simple) {
			return isCommand("SCAN ENTITY", input);
		}
		
		Pattern pattern = Pattern.compile(ScanEntityWithWatchParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(ScanEntityWithWatchParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}
	
	/**
	 * 解析“SCAN ENTITY”语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回ScanEntity命令
	 */
	public ScanEntity split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		ScanEntity cmd = new ScanEntity();
		// 第一种
		Pattern pattern = Pattern.compile(ScanEntityWithWatchParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			Space space = new Space(matcher.group(1), matcher.group(2));
			cmd.setSpace(space);
			// 后缀参数
			String suffix = matcher.group(3);
			if (!isAllKeyword(suffix)) {
				List<Node> sites = splitSites(suffix, SiteTag.DATA_SITE);
				cmd.addSites(sites);
			}
		}
		// 第二种
		if (!match) {
			pattern = Pattern.compile(ScanEntityWithWatchParser.REGEX2);
			matcher = pattern.matcher(input);
			if (match = matcher.matches()) {
				Space space = new Space(matcher.group(1), matcher.group(2));
				cmd.setSpace(space);
			}
		}
		// 不匹配是错误
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 在线，检查表名有效
		if (online) {
			if (!hasTable(cmd.getSpace())) {
				throwableNo(FaultTip.NOTFOUND_X, cmd.getSpace());
			}
		}

		cmd.setPrimitive(input);
		return cmd;
	}

}