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
 * 恢复表数据一致性解析器 <br><br>
 * 
 * 语句格式：RECOVER ENTITY CONSISTENCY 数据库名.表名 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class RecoverEntityConsistencyParser extends SyntaxParser {

	/** 正则表达式 **/
	private static final String REGEX = "^\\s*(?i)(?:RECOVER\\s+ENTITY\\s+CONSISTENCY)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造恢复表数据一致性解析器
	 */
	public RecoverEntityConsistencyParser() {
		super();
	}

	/**
	 * 判断匹配“RECOVER ENTITY CONSISTENCY”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RECOVER ENTITY CONSISTENCY", input);
		}
		Pattern pattern = Pattern.compile(RecoverEntityConsistencyParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“RECOVER ENTITY CONSISTENCY”语句
	 * @param input 输入语句
	 * @param online 在线模式（检测参数正确）
	 * @return 返回RecoverEntityConsistency命令
	 */
	public RecoverEntityConsistency split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(RecoverEntityConsistencyParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		// 取出参数
		String suffix = matcher.group(1);
		// 判断表名正确
		if (!Space.validate(suffix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, suffix);
		}

		Space space = new Space(suffix);
		// 如果是在线模式，检查表存在
		if (online) {
			boolean success = hasTable(space);
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		RecoverEntityConsistency cmd = new RecoverEntityConsistency(space);
		// 保存命令原语
		cmd.setPrimitive(input);
		return cmd;
	}

}