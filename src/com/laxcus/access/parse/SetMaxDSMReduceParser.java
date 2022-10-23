/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 设置DSM表压缩倍数解析器。<br><br>
 * 
 * 语法:SET MAX DSM REDUCE 数据库名.表名   压缩倍数 [TO data site, data site...] <BR>
 * 
 * 管理员或者有管理员身份的用户操作。
 * 
 * @author scott.liang
 * @version 1.0 5/21/2019
 * @since laxcus 1.0
 */
public class SetMaxDSMReduceParser extends SyntaxParser {

	/** SET MAX DSM REDUCE 正则表达式 **/
	private final static String REGEX1 = "^\\s*(?i)(?:SET\\s+MAX\\s+DSM\\s+REDUCE)\\s+(?i)([\\w\\W]+|[SIGN\\s+\\w\\W]+)\\s+([\\w\\W]+)\\s+([1-9][0-9]*)\\s*$";

	/**
	 * 构造默认的设置DSM表压缩倍数解析器
	 */
	public SetMaxDSMReduceParser() {
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
			return isCommand("SET MAX DSM REDUCE", input);
		}
		Pattern pattern = Pattern.compile(SetMaxDSMReduceParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析“SET MAX DSM REDUCE ...”语句
	 * @param input 输入语句
	 * @return 返回SetDSMReduce命令实例
	 */
	public SetMaxDSMReduce split(String input) {
		Pattern pattern = Pattern.compile(SetMaxDSMReduceParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		// 解析用户签名
		Siger siger = splitSiger(matcher.group(1));
		// 表名 
		String prefix = matcher.group(2);
		int multiple = Integer.parseInt(matcher.group(3));
		
		// 检查表名
		if (!Space.validate(prefix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		Space space = new Space(prefix);
		SetMaxDSMReduce cmd = new SetMaxDSMReduce(siger, space, multiple);
	
		cmd.setPrimitive(input); //原语
		return cmd;
	}
}
