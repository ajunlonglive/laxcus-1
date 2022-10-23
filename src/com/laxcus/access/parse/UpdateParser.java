/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.*;
import com.laxcus.util.tip.*;

/**
 * SQL UPDATE 语句解析器
 * 
 * 语句：UPDATE 数据库名.表名  SET (列名=... , 列名 =... ) WHERE 比较语句
 * 
 * @author scott.liang
 * @version 1.0 9/25/2009
 * @since laxcus 1.0
 */
public class UpdateParser extends SyntaxParser {

//	/** 更新记录: UPDATE schema.table SET new_column_name=new_value, ... WHERE old_column_name=old_value [AND|OR] ... */
//	private final static String SQL_UPDATE = "^\\s*(?i)UPDATE\\s+(\\w+)\\.(\\w+)\\s+(?i)SET\\s+([\\w\\W]+?)\\s+(?i)WHERE\\s+([\\w\\W]+)\\s*$";
//	
//	/** UPDATE SET 语句格式  */
//	private final static String SPLIT_COMMA = "^\\s*\\,\\s*([\\w\\W]+)$";
//	private final static String SPLIT_NAME = "^\\s*(\\w+?)(\\s*\\=\\s*[\\w\\W]+)$";
//	private final static String SQL_RAW = "^\\s*(\\w+?)\\s*\\=\\s*(?i)0x([0-9a-fA-F]+)(\\s*\\,\\s*\\w+[\\w\\W]+|\\s*)$";
//	private final static String SQL_STRING = "^\\s*(\\w+?)\\s*\\=\\s*\\'([\\w\\W]+?)\\'(\\s*\\,\\s*\\w+\\s*[\\w\\W]+|\\s*)$";
//	private final static String SQL_NUMBER = "^\\s*(\\w+?)\\s*\\=\\s*([+|-]{0,1}[0-9]+[\\\\.]{0,1}[0-9]*)(\\s*\\,\\s*\\w+\\s*[\\w\\W]+|\\s*)$";

	
	/** 更新记录: UPDATE schema.table SET （new_column_name=new_value, ...） WHERE old_column_name=old_value [AND|OR] ... */
	private final static String SQL_UPDATE = "^\\s*(?i)UPDATE\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)\\s+(?i)(?:SET)\\s+\\(\\s*([\\w\\W]+?)\\)\\s+(?i)WHERE\\s+([\\w\\W]+)\\s*$";

	/** UPDATE SET 语句格式  */
	private final static String SPLIT_COMMA = "^\\s*\\,\\s*([\\w\\W]+)$";
	private final static String SPLIT_NAME = "^\\s*([\\w\\W]+?)(\\s*\\=\\s*[\\w\\W]+)$";
	private final static String SQL_RAW = "^\\s*([\\w\\W]+?)\\s*\\=\\s*(?i)0x([0-9a-fA-F]+)(\\s*\\,.+|\\s*)$";
	private final static String SQL_STRING = "^\\s*([\\w\\W]+?)\\s*\\=\\s*\\'([\\w\\W]+?)\\'(\\s*\\,.+|\\s*)$";
	private final static String SQL_NUMBER = "^\\s*([\\w\\W]+?)\\s*\\=\\s*([+|-]{0,1}[0-9]+[\\\\.]{0,1}[0-9]*)(\\s*\\,.+|\\s*)$$";

	/**
	 * 构造默认的UPDATE语句解析器
	 */
	public UpdateParser() {
		super();
	}
	
	/**
	 * 检查匹配SQL “UPDATE SET ”语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("UPDATE", input);
		}
		Pattern pattern = Pattern.compile(UpdateParser.SQL_UPDATE);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析 UPDATE scheme.table SET (...) WHERE 语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回Update命令
	 */
	public Update split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(UpdateParser.SQL_UPDATE);
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		Space space = new Space(matcher.group(1), matcher.group(2));
		ResourceChooser chooser = SyntaxParser.getResourceChooser();
		Table table = chooser.findTable(space);
		if (table == null) {
			throwableNo(FaultTip.NOTFOUND_X, space);
		}
		// 在线模式， 判断有UPDATE权限
		if (online) {
			boolean allow = chooser.canUpdate(space);
			if (!allow) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}
				
		String sqlSet = matcher.group(3);
		String sqlWhere = matcher.group(4);

		Update update = new Update(space);
		// 解析更新字段
		try {
			splitSet(table, sqlSet, update);
		} catch(IOException e) {
			throw new SyntaxException(e);
		}
		// 解析WHERE语句
		WhereParser parser = new WhereParser();
		Where condi = parser.split(table, sqlWhere, online);
		update.setWhere(condi);
		
		// 保留原语
		update.setPrimitive(input);
		
		return update;
	}
	
	/**
	 * 解析UPDATE SET语句中的列参数集合
	 * 
	 * @param table
	 * @param input
	 * @param update
	 */
	private void splitSet(Table table, String input, Update update) throws IOException {
		Pattern comma = Pattern.compile(UpdateParser.SPLIT_COMMA);
		Pattern key = Pattern.compile(UpdateParser.SPLIT_NAME);
		
		for (int index = 0; input.trim().length() > 0; index++) {
			// 过滤分隔符逗号
			if (index > 0) {
				Matcher matcher = comma.matcher(input);
				if (!matcher.matches()) {
//					throw new SyntaxException("illegal values:%s", input);
					throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, input);
				}
				input = matcher.group(1);
			}
			// 取列名称
			Matcher matcher = key.matcher(input);
			if (!matcher.matches()) {
//				throw new SyntaxException("invalid sql:%s", input);
				throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
			}
			String name = matcher.group(1);
			// 根据名称，找到匹配的属性
			ColumnAttribute attribute = table.find(name);
			if (attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, name);
			}
			
			Column column = null;
			if(attribute.isRaw()) { 
				// 二进制数组格式
				Pattern pattern = Pattern.compile(UpdateParser.SQL_RAW);
				matcher = pattern.matcher(input);
				if (!matcher.matches()) {
					// throw new SyntaxException("illegal raw:%s", input);
					throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
				}
				String value = matcher.group(2);
				input = matcher.group(3);
				column = VariableGenerator.createRaw(table.isDSM(), (RawAttribute) attribute, value);
			} else if(attribute.isCalendar() || attribute.isWord()) { 
				// 字符串格式，包括日期和字符
				Pattern pattern = Pattern.compile(UpdateParser.SQL_STRING);
				matcher = pattern.matcher(input);
				if (!matcher.matches()) {
					// throw new SyntaxException("illegal string:%s", input);
					throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
				}
				String value = matcher.group(2);
				input = matcher.group(3);

				if (attribute.isChar()) {
					column = VariableGenerator.createChar(table.isDSM(), (CharAttribute) attribute, value);
				} else if (attribute.isWChar()) {
					column = VariableGenerator.createWChar(table.isDSM(), (WCharAttribute) attribute, value);
				} else if (attribute.isHChar()) {
					column = VariableGenerator.createHChar(table.isDSM(), (HCharAttribute) attribute, value);
				} else if (attribute.isDate()) {
					column = CalendarGenerator.createDate((DateAttribute) attribute, value);
				} else if (attribute.isTime()) {
					column = CalendarGenerator.createTime((TimeAttribute) attribute, value);
				} else if (attribute.isTimestamp()) {
					column = CalendarGenerator.createTimestamp((TimestampAttribute) attribute, value);
				}
			} else if(attribute.isNumber()) {
				// 数字格式
				Pattern pattern = Pattern.compile(UpdateParser.SQL_NUMBER);
				matcher = pattern.matcher(input);
				if (!matcher.matches()) {
					//	throw new SyntaxException("illegal number:%s", input);
					throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
				}
				String value = matcher.group(2);
				input = matcher.group(3);

				if (attribute.isShort()) {
					column = NumberGenerator.createShort((ShortAttribute) attribute, value);
				} else if (attribute.isInteger()) {
					column = NumberGenerator.createInteger((IntegerAttribute)attribute, value);
				} else if (attribute.isLong()) {
					column = NumberGenerator.createLong((LongAttribute)attribute, value);
				} else if (attribute.isFloat()) {
					column = NumberGenerator.createFloat((FloatAttribute) attribute, value);
				} else if (attribute.isDouble()) {
					column = NumberGenerator.createDouble((DoubleAttribute) attribute, value);
				}
			}
			if (column == null) {
				throwableNo(FaultTip.NOTRESOLVE_X, input);
			}
			// 设置列编号
			column.setId(attribute.getColumnId());
			// 保存一列数据
			update.add(column);
		}
	}

}