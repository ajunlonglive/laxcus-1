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
import com.laxcus.command.access.table.*;
import com.laxcus.util.tip.*;

/**
 * 检查表数据一致性解析器。<br><br>
 * 
 * 语句格式：<BR>
 * 1. CHECK ENTITY CONSISTENCY 数据库.表  DETAIL <BR>
 * 2. CHECK ENTITY CONSISTENCY 数据库.表 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class CheckEntityConsistencyParser extends SyntaxParser {
	
	/** 正则表达式，要求详细的记录  **/
	private static final String REGEX1 = "^\\s*(?i)(?:CHECK\\s+ENTITY\\s+CONSISTENCY)\\s+([\\w\\W]+)\\s+(?i)(?:DETAIL)\\s*$";

	/** 正则表达式 **/
	private static final String REGEX2 = "^\\s*(?i)(?:CHECK\\s+ENTITY\\s+CONSISTENCY)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造检查表数据一致性解析器
	 */
	public CheckEntityConsistencyParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK ENTITY CONSISTENCY", input);
		}
		Pattern pattern = Pattern.compile(CheckEntityConsistencyParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			pattern = Pattern.compile(CheckEntityConsistencyParser.REGEX2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 解析语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回CheckEntityConsistency命令
	 */
	public CheckEntityConsistency split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		boolean detail = false;
		
		Pattern pattern = Pattern.compile(CheckEntityConsistencyParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			detail = true;
		} else {
			pattern = Pattern.compile(CheckEntityConsistencyParser.REGEX2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		// 以上不成功时...
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		
		// 取出数据表名
		String suffix = matcher.group(1);
		// 检查表名正确
		if (!Space.validate(suffix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, suffix);
		}

		// 表名
		Space space = new Space(suffix);
		// 如果是在线模式，检查表存在
		if (online) {
			success = hasTable(space);
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		CheckEntityConsistency cmd = new CheckEntityConsistency(space);
		cmd.setDetail(detail);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

	public static void main(String[] args) {
		CheckEntityConsistencyParser e = new CheckEntityConsistencyParser();
		String input = "CHeck entity consistency 数据库DB.数据表TABLE detail ";
		CheckEntityConsistency cmd = e.split(input, false);
		System.out.println(cmd.getPrimitive());
		System.out.printf("detail is %s\n", cmd.isDetail());
	}
}
