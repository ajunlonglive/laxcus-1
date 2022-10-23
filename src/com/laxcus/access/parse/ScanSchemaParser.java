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
 * 扫描数据库命令解析器。<br>
 * 语句格式：SCAN DATABASE [数据库名1, 数据库名2, ...] TO [ALL | DATA站点1, DATA站点2, ... ] <br>
 * 
 * “SCAN DATABASE”分别被WATCH/FRONT两种节点使用。FRONT节点不需要输入“TO”后面的参数，WATCH节点可选（输入或者不输入）。
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class ScanSchemaParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SCAN\\s+DATABASE)\\s+([\\w\\W]+)\\s*$";
	
	/** SCAN DATABASE 语句格式1 **/
	private final static String REGEX1 = "^\\s*(?i)(?:SCAN\\s+DATABASE)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/** SCAN DATABASE 语句格式2 **/
	private final static String REGEX2 = "^\\s*(?i)(?:SCAN\\s+DATABASE)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造扫描数据库命令解析器
	 */
	public ScanSchemaParser() {
		super();
	}

	/**
	 * 检查输入语句匹配本命令
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SCAN DATABASE", input);
		}
		Pattern pattern = Pattern.compile(ScanSchemaParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(ScanSchemaParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}
	
	/**
	 * 解析数据库名称名称
	 * @param input 命令原语
	 * @param online 在线模式。当处于在线模式时，需要检查数据库名称且属于这个用户。
	 * @return 返回数据库名称名称列表
	 */
	private List<Fame> splitSchemas(String input, boolean online) {
		ArrayList<Fame> array = new ArrayList<Fame>();
				
		// 解析数据库名称名称，数据库名称名称以逗号分割
		String[] items = splitCommaSymbol(input);
		for (String item : items) {
			// 判断语法正确
			if (!Fame.validate(item)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}
			Fame fame = new Fame(item);
			// 判断数据库存在
			if (online) {
				boolean success = hasSchema(fame);
				if (!success) {
					throwableNo(FaultTip.NOTFOUND_X, item);
				}
			}
			// 保存
			array.add(fame);
		}
		
		return array;
	}
	
	/**
	 * 解析ScanSchema命令
	 * @param input ScanSchema命令语句
	 * @param online 在线模式。当处于在线模式时，需要检查数据库名称且属于这个用户。
	 * @return 返回ScanSchema命令
	 */
	public ScanSchema split(String input, boolean online) {	
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		ScanSchema cmd = new ScanSchema();

		// 第一种格式
		Pattern pattern = Pattern.compile(ScanSchemaParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			List<Fame> fames = splitSchemas(matcher.group(1), online);
			cmd.addAll(fames);
			// 后缀参数
			String suffix = matcher.group(2);
			if (!isAllKeyword(suffix)) {
				List<Node> sites = splitSites(suffix, SiteTag.DATA_SITE);
				cmd.addSites(sites);
			}
		}
		// 第二种格式
		if (!match) {
			pattern = Pattern.compile(ScanSchemaParser.REGEX2);
			matcher = pattern.matcher(input);
			if (match = matcher.matches()) {
				List<Fame> fames = splitSchemas(matcher.group(1), online);
				cmd.addAll(fames);
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