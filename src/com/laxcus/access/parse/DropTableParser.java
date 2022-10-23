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
 * 删除表命令解析器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2009
 * @since laxcus 1.0
 */
public class DropTableParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:DROP\\s+TABLE)\\s+([\\w\\W]+)\\s*$";

	/** 删除表。格式: DROP TABLE schema.table **/
	private final static String DROP_TABLE = "^\\s*(?i)(?:DROP\\s+TABLE)\\s+([\\w\\u4e00-\\u9fa5]+)\\.([\\w\\u4e00-\\u9fa5]+)\\s*$";

	/**
	 * 构造删除表命令解析器
	 */
	public DropTableParser() {
		super();
	}

	/**
	 * 判断匹配删除数据表语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DROP TABLE", input);
		}
		Pattern pattern = Pattern.compile(DropTableParser.DROP_TABLE);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析删除数据表语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回DropTable命令
	 */
	public DropTable split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(DropTableParser.DROP_TABLE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		Space space = new Space(matcher.group(1), matcher.group(2));
		if (online) {
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
		}
		
		DropTable cmd = new DropTable(space);
		cmd.setPrimitive(input);
		return cmd;
	}

}
