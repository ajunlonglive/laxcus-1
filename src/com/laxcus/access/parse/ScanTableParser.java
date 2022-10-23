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
 * 扫描数据表命令解析器。<br>
 * 语句格式：SCAN TABLE [数据表名1, 数据表名2, ...] TO [ALL | DATA站点1, DATA站点2, ... ] <br>
 * 
 * “SCAN TABLE”分别被WATCH/FRONT两种节点使用。FRONT节点不需要输入“TO”后面的参数，WATCH节点可选（输入或者不输入）。
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class ScanTableParser extends SyntaxParser {
	
//	private static final String REGEX_TITLE = "^\\s*(?i)(?:SCAN\\s+TABLE)\\s+([\\w\\W]+)\\s*$";

	/** SCAN TABLE 语句格式1 **/
	private static final String REGEX1 = "^\\s*(?i)(?:SCAN\\s+TABLE)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/** SCAN TABLE 语句格式2 **/
	private static final String REGEX2 = "^\\s*(?i)(?:SCAN\\s+TABLE)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造扫描数据表命令解析器
	 */
	public ScanTableParser() {
		super();
	}

	/**
	 * 检查输入语句匹配本命令
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SCAN TABLE", input);
		}
		Pattern pattern = Pattern.compile(ScanTableParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(ScanTableParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析ScanTable命令
	 * @param input ScanTable命令语句
	 * @param online 在线模式。当处于在线模式时，需要检查表空间存在且属于这个用户。
	 * @return 返回ScanTable命令
	 */
	public ScanTable split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		ScanTable cmd = new ScanTable();

		// 第一种格式
		Pattern pattern = Pattern.compile(ScanTableParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			List<Space> spaces = splitSpaces(matcher.group(1), online);
			cmd.addAll(spaces);
			// 后缀参数
			String suffix = matcher.group(2);
			if (!isAllKeyword(suffix)) {
				List<Node> sites = splitSites(suffix, SiteTag.DATA_SITE);
				cmd.addSites(sites);
			}
		}
		// 第二种格式
		if (!match) {
			pattern = Pattern.compile(ScanTableParser.REGEX2);
			matcher = pattern.matcher(input);
			if (match = matcher.matches()) {
				List<Space> spaces = splitSpaces(matcher.group(1), online);
				cmd.addAll(spaces);
			}
		}
		// 以上不正确是错误
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX, input);
		}

		// 保存原语
		cmd.setPrimitive(input);

		return cmd;
	}
}
