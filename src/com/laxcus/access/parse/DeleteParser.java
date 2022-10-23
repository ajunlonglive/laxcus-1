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
import com.laxcus.command.access.*;
import com.laxcus.util.tip.*;

/**
 * SQL DELETE语句解析器
 * 
 * 删除语句：DELETE FROM 数据库名.表名  WHERE 检索条件 
 * 
 * @author scott.liang
 * @version 1.0 9/25/2009
 * @since laxcus 1.0
 */
public class DeleteParser extends SyntaxParser {

	/** 删除命令 **/
	private final static String SQL_DELETE = "^\\s*(?i)(?:DELETE\\s+FROM)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(?:WHERE)\\s+([\\w\\W]+)\\s*$";

//	/** 删除命令  **/
//	private final static String SQL_DELETE = "^\\s*(?i)(?:DELETE\\s+FROM)\\s+([\\w\\W]+?)\\.([\\w\\W]+?)\\s+(?i)(?:WHERE)\\s+([\\w\\W]+)\\s*$";
	
	/**
	 * 构造DELETE语句解析器
	 */
	public DeleteParser() {
		super();
	}

	/**
	 * 检查传入的语句匹配DELETE语法
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DELETE FROM", input);
		}
		Pattern pattern = Pattern.compile(DeleteParser.SQL_DELETE);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析 DELETE FROM ... 语句
	 * @param input 输入语句
	 * @return 返回Delete实例
	 */
	public Delete split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(DeleteParser.SQL_DELETE);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		Space space = new Space(matcher.group(1), matcher.group(2));
		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			throwableNo(FaultTip.NOTFOUND_X, space);
		}
		
		// 在线模式，判断有DELETE权限
		if (online) {
			boolean allow = chooser.canDelete(space);
			if (!allow) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		String sqlWhere = matcher.group(3);
		Delete delete = new Delete(space);
		// 解析WHERE语句
		WhereParser parser = new WhereParser();
		Where condi = parser.split(table, sqlWhere, online);
		delete.setWhere(condi);

		// 保存原语
		delete.setPrimitive(input);

		return delete;
	}
	
//	public static void main(String[] args) {
//		String input = "delete from 媒体库_历史.音乐  WHERE id>0";
//		DeleteParser e = new DeleteParser();
//		boolean success = e.matches(input);
//		System.out.println(DeleteParser.SQL_DELETE);
//		System.out.printf("match is %s\n", success);
//	}
}