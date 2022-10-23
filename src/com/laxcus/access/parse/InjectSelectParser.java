/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.util.tip.*;

/**
 * 基于SELECT查询基础的数据插入解析器
 * 
 * @author scott.liang
 * @version 1.0 11/27/2020
 * @since laxcus 1.0
 */
public class InjectSelectParser extends SyntaxParser {
	
	private final static String REGEX = "^\\s*(?i)(?:INJECT\\s+INTO)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\(([\\w\\W]+?)\\)\\s+(?i)(SELECT\\s+[\\w\\W]+)\\s*$";

	/**
	 * 构造默认的基于SELECT查询基础的数据插入解析器
	 */
	public InjectSelectParser() {
		super();
	}
	
	/**
	 * 判断语句匹配
	 * @param input
	 * @return
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(InjectSelectParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 检查INJECT INTO 后面的列名称
	 * @param table
	 * @param input
	 * @return
	 */
	private ListSheet splitShowElement(Table table, String input) {
		ListSheet sheet = new ListSheet();		
		
		String[] fields = input.split("\\s*\\,\\s*");
		
		for(int index =0; index < fields.length; index++) {
			String name = fields[index];
			
			ColumnAttribute attribute = table.find(name); // 列名
			if (attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, name);
			}
			// 如果有成员存在时是错误
			if (sheet.contains(table.getSpace(), attribute.getColumnId())) {
				throwableNo(FaultTip.SQL_OVERLAP_COLUMN_X, name);
			}
			// 生成列成员
			ColumnElement element = new ColumnElement(table.getSpace(), attribute.getTag());
			sheet.add(element);
		}
		
		return sheet;
	}
	
	/**
	 * 检查列参数一致性
	 * @param left
	 * @param right
	 */
	private void check(ListSheet left, ListSheet right) {
		int size = left.size();
		if (size != right.size()) {
			String s = String.format("%d != %d", size, right.size());
			throwableNo(FaultTip.NOTMATCH_X, s);
		}
		
		// 检查属性
		for (int index = 0; index < size; index++) {
			ListElement e1 = left.get(index);
			// 不允许是函数
			if(e1.isFunction()) {
				throwableNo(FaultTip.NOTSUPPORT_X, e1.getName());
			}
			ListElement e2 = right.get(index);
			// 暂时不允许是函数
			if (e2.isFunction()) {
				throwableNo(FaultTip.NOTSUPPORT_X, e2.getName());
			}
			// 类型不一致时弹出异常
			if (e1.getType() != e2.getType()) {
				String s = String.format("%s != %s", e1.getName(), e2.getName());
				throwableNo(FaultTip.NOTMATCH_X, s);
			}
		}
	}
	
	/**
	 * 解析语句
	 * @param input 输入语句
	 * @param online 在线模式或者否
	 * @return 返回解析后的命令
	 */
	public InjectSelect split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		// 检查语句
		Pattern pattern = Pattern.compile(InjectSelectParser.REGEX);
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

		// 列参数名称
		String fields = matcher.group(3);
		// SELECT查询语句
		String select = matcher.group(4);
		
		// 生成命令
		InjectSelect cmd = new InjectSelect(space);
		// 解析插入命令
		ListSheet sheet = this.splitShowElement(table, fields);
		
		// 解析SELECT语句
		SelectParser parser = new SelectParser();
		Select sub = parser.split(select, online);
		ListSheet right = sub.getListSheet();

		// 检查一致性
		check(sheet, right);
		
		// 设置参数
		cmd.setListSheet(sheet);
		cmd.setSelect(sub);
		// 保存命令原语
		cmd.setPrimitive(input);
		
		return cmd;
	}
}
