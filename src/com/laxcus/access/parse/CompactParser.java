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
import com.laxcus.command.rebuild.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 缓存数据块压缩命令解析器。<br><br>
 * 
 * 删除CACHE状态数据块中的冗余数据。<br>
 * 
 * COMPACT命令只允许系统管理员使用，通过WATCH站点操作，只能用于测试目的，生产环境禁止使用。<br>
 * 
 * 因为是用于测试，本语法检查忽略它的数据表名有效性检查。<br><br>
 * 
 * 语法:COMPACT 数据库名.表名 [TO address, address...] <BR>
 * 
 * @author scott.liang
 * @version 1.0 3/28/2018
 * @since laxcus 1.0
 */
public class CompactParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:COMPACT)\\s+([\\w\\W]+)\\s*$";
	
	/** COMPACT 正则表达式 **/
	private final static String REGEX1 = "^\\s*(?i)(?:COMPACT)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)$";

	private final static String REGEX2 = "^\\s*(?i)(?:COMPACT)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造数据块强制转换命令解析器
	 */
	public CompactParser() {
		super();
	}

	/**
	 * 检查强制转化命令是否匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("COMPACT", input);
		}
		Pattern pattern = Pattern.compile(CompactParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(CompactParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析“COMPACT ...”语句
	 * @param input 输入语句
	 * @return 返回Compact命令实例
	 */
	public Compact split(String input) {
		String prefix = null;
		String suffix = null;

		Pattern pattern = Pattern.compile(CompactParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (match) {
			prefix = matcher.group(1);
			suffix = matcher.group(2);
		}
		if (!match) {
			pattern = Pattern.compile(CompactParser.REGEX2);
			matcher = pattern.matcher(input);
			if (match = matcher.matches()) {
				prefix = matcher.group(1);
			}
		}
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 检查表名
		if (!Space.validate(prefix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		Space space = new Space(prefix);
		Compact cmd = new Compact(space);
		// 解析指定的DATA主站点地址
		if (suffix != null) {
			List<Node> list = splitSites(suffix, SiteTag.DATA_SITE);
			cmd.addAll(list);
		}

		cmd.setPrimitive(input); //原语
		return cmd;
	}

}