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
 * DSM表压缩倍数解析器。<br><br>
 * 
 * 语法:SET DSM REDUCE 数据库名.表名   压缩倍数 [TO data site, data site...] <BR>
 * 
 * @author scott.liang
 * @version 1.0 5/21/2019
 * @since laxcus 1.0
 */
public class SetDSMReduceParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:SET\\s+DSM\\s+REDUCE)\\s+([\\w\\W]+)\\s*$";
	
	/** SET DSM REDUCE 正则表达式 **/
	private final static String REGEX1 = "^\\s*(?i)(?:SET\\s+DSM\\s+REDUCE)\\s+([\\w\\W]+)\\s+([1-9][0-9]*)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	private final static String REGEX2 = "^\\s*(?i)(?:SET\\s+DSM\\s+REDUCE)\\s+([\\w\\W]+)\\s+([1-9][0-9]*)\\s*$";

	/**
	 * 构造DSM表压缩倍数解析器
	 */
	public SetDSMReduceParser() {
		super();
	}

	/**
	 * 检查输入语句是否匹配
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET DSM REDUCE", input);
		}
		Pattern pattern = Pattern.compile(SetDSMReduceParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(SetDSMReduceParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析“SET DSM REDUCE ...”语句
	 * @param input 输入语句
	 * @return 返回SetDSMReduce命令实例
	 */
	public SetDSMReduce split(String input) {
		String prefix = null;
		int multiple = 0;
		String suffix = null;

		Pattern pattern = Pattern.compile(SetDSMReduceParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			prefix = matcher.group(1);
			multiple = Integer.parseInt(matcher.group(2));
			suffix = matcher.group(3);
		}
		if (!success) {
			pattern = Pattern.compile(SetDSMReduceParser.REGEX2);
			matcher = pattern.matcher(input);
			if (success = matcher.matches()) {
				prefix = matcher.group(1);
				multiple = Integer.parseInt(matcher.group(2));
			}
		}
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		// 检查表名
		if (!Space.validate(prefix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		Space space = new Space(prefix);
		SetDSMReduce cmd = new SetDSMReduce(space, multiple);
		// 解析指定的DATA主站点地址
		if (suffix != null) {
			List<Node> list = splitSites(suffix, SiteTag.DATA_SITE);
			cmd.addAll(list);
		}

		cmd.setPrimitive(input); //原语
		return cmd;
	}

}