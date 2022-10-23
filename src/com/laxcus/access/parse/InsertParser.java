/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.*;
import com.laxcus.util.tip.*;

/**
 * 数据插入语句解析器。解析“INSERT INTO/INJECT INTO”语句。
 * 
 * @author scott.liang
 * @version 1.1 12/10/2009
 * @since laxcus 1.0
 */
public class InsertParser extends SyntaxParser {

	/** 插入一行记录: INSERT INTO SCHEMA.TABLE (column_name1, ...) VALUES (column1,...) */
	private final static String INSERT = "^\\s*(?i)(?:INSERT\\s+INTO)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\(([\\w\\W]+?)\\)\\s*(?i)VALUES\\s*\\(([\\w\\W]+)\\)\\s*$";
	
	/** 插入多行记录: INJECT INTO SCHEMA.TABLE (column_name1,....) VALUES (column1,...),(column1,...),(column1,...) */
	private final static String INJECT = "^\\s*(?i)(?:INJECT\\s+INTO)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\.([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\(([\\w\\W]+?)\\)\\s*(?i)VALUES\\s*([\\w\\W]+)\\s*$";

	/** 分割INJECT INTO的 VALUES域 */
	private final static String SPLIT_COMMA = "^\\s*\\,\\s*([\\w\\W]+)$";
	private final static String SPLIT_VALUES = "^\\s*\\(([\\w\\W]+?)\\)(\\s*\\,\\s*\\([\\w\\W]+|\\s*)$";
	
	/** 值参数格式  */
	private final static String RAW = "^\\s*(?i)0x([0-9a-fA-F]+)(\\s*\\,[\\w\\W]+|\\s*)$";
	private final static String STRING = "^\\s*\\'([\\w\\W]+?)\\'(\\s*\\,[\\w\\W]+|\\s*)$";
	private final static String NUMBER = "^\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]*)(\\s*\\,[\\w\\W]+|\\s*)$";
	// 科学计数法
	private final static String NUMBER2 = "^\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]+E\\+[0-9]+)(\\s*\\,[\\w\\W]+|\\s*)$";

	/**
	 * 构造数据插入语句解析器
	 */
	public InsertParser() {
		super();
	}
	
	/**
	 * 检查传入的语法是否匹配INSERT INTO语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isInsert(String input) {
		Pattern pattern = Pattern.compile(InsertParser.INSERT);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 检查传入的语法是否匹配INJECT INTO语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean isInject(String input) {
		Pattern pattern = Pattern.compile(InsertParser.INJECT);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 过滤两侧的括号和括号之间的逗号，返回字符串数组
	 * @param input
	 * @return
	 */
	private String[] splitValues(String input) {
		ArrayList<String> array = new ArrayList<String>();

		Pattern comma = Pattern.compile(InsertParser.SPLIT_COMMA);
		Pattern pattern = Pattern.compile(InsertParser.SPLIT_VALUES);
		Matcher matcher = pattern.matcher(input);
		// 第一段前面必须没有逗号
		if (!matcher.matches()) {
			throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, input);
//			throw new SyntaxException("illegal values:%s", input);
		}
		array.add(matcher.group(1));
		input = matcher.group(2);
		// 后叙段前面必须有逗号
		while (input.trim().length() > 0) {
			// 过滤 检查前面的逗号
			matcher = comma.matcher(input);
			if (!matcher.matches()) {
				throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, input);
//				throw new SyntaxException("illegal values:%s", input);
			}
			input = matcher.group(1);

			// 判断字符串是否匹配
			matcher = pattern.matcher(input);
			if (!matcher.matches()) {
				throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, input);
//				throw new SyntaxException("illegal values:%s", input);
			}
			array.add(matcher.group(1));
			input = matcher.group(2);
		}

		String[] s = new String[array.size()];
		return array.toArray(s);
	}
	
	/**
	 * 一行记录中，如果有某列不存在，定义一个默认值
	 * @param row
	 * @param table
	 */
	private void fill(Row row, Table table) {
		for (ColumnAttribute attribute : table.list()) {
			short columnId = attribute.getColumnId();
			Column column = row.find(columnId);
			if (column != null) continue;
			// 生成一个默认值
			column = attribute.getDefault();
			if (column == null) {
//				throw new SyntaxException("%s cannot support default", attribute.getNameText());
				throwableNo(FaultTip.SQL_CANNOTSUPPORT_X, attribute.getNameText());
			}
			row.add(column);
		}
	}
	
	/**
	 * 解析一行记录，返回Row对象
	 * @param table 数据表
	 * @param names 列名
	 * @param input 输入参数
	 * @return 返回一行数据
	 */
	private Row splitItem(Table table, String[] names, String input) throws IOException {
		Pattern comma = Pattern.compile(InsertParser.SPLIT_COMMA);
		Row row = new Row();
		for(int i = 0; i < names.length; i++) {
			// 根据名称，找到匹配的属性
			ColumnAttribute attribute = table.find(names[i]);
			if(attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, names[i]);
			}
			// 第二行及以后需要去掉逗号
			if (i > 0) {
				Matcher matcher = comma.matcher(input);
				if (!matcher.matches()) {
					throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, input);
//					throw new SyntaxException("illegal prefix: %s", input);
				}
				input = matcher.group(1);
			}
			// 参数不足
			if (input.trim().isEmpty()) {
//				throw new SyntaxException("values missing!");
				throwable(FaultTip.PARAMETER_MISSING);
			}
			
			Column column = null;
			if (attribute.isRaw()) {
				// 二进制格式
				Pattern pattern = Pattern.compile(InsertParser.RAW);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
//					throw new SyntaxException("illegal raw:%s", input);
					throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
				}
				String value = matcher.group(1);
				input = matcher.group(2);
				column = VariableGenerator.createRaw(table.isDSM(), (RawAttribute) attribute, value);
			} else if (attribute.isCalendar() || attribute.isWord()) {
				// 字符串格式，按照字符串格式分解
				Pattern pattern = Pattern.compile(InsertParser.STRING);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
//					throw new SyntaxException("illegal string:%s", input);
					throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
				}
				String value = matcher.group(1);
				input = matcher.group(2);
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
			} else if (attribute.isNumber()) {
				Pattern pattern = Pattern.compile(InsertParser.NUMBER);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
					pattern = Pattern.compile(InsertParser.NUMBER2);
					matcher = pattern.matcher(input);
					// 解析出错，报告
					if (!matcher.matches()) {
//						throw new SyntaxException("illegal number:%s", input);
						throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
					}
				}
				
				String value = matcher.group(1);
				input = matcher.group(2);
				
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
			
			if(column == null) {
				throwableNo(FaultTip.NOTRESOLVE_X, input);
			}
			
			column.setId(attribute.getColumnId());
			row.add(column);
		}
		
		// 填充没有的列
		fill(row, table);
		// 根据列的标识号排序
		row.aligment();
		
		return row;
	}
	
	/**
	 * 分割属性名称
	 * 
	 * @param fields
	 * @return
	 */
	private String[] splitFieldNames(String fields) {
		return fields.split("\\s*\\,\\s*");
	}
	
	/**
	 * 解析"INSERT INTO"语句
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回Insert命令
	 */
	public Insert splitInsert(String input, boolean online) {		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(InsertParser.INSERT);
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
		
		// 如果是在线状态，判断有INSERT权限
		if (online) {
			boolean allow = chooser.canInsert(space);
			if (!allow) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}

		String fields = matcher.group(3);
		String values = matcher.group(4);

		String[] names = splitFieldNames(fields);
		Row row = null;
		try {
			row = splitItem(table, names, values);
		} catch (IOException e) {
			throw new SyntaxException(e);
		}

		Insert insert = new Insert(space);
		insert.add(row);
		return insert;
	}

	/**
	 * 解析"INJECT INTO"语句
	 * @param input 输入语句
	 * @return 返回Insert命令
	 */
	public Insert splitInject(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(InsertParser.INJECT);
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
		
		// 在线模式，判断有INSERT权限
		if (online) {
			boolean allow = chooser.canInsert(space);
			if (!allow) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, space);
			}
		}
		
		String fields = matcher.group(3);
		String values = matcher.group(4);
		
		String[] names = splitFieldNames(fields);
		String[] items = splitValues(values);
		
		Insert cmd = new Insert(space);
		
		// 解析"INJECT INTO"的VALUES域
		for (String line : items) {
			// 表、属性名称、值集合，三项条件生成一行记录
			try {
				Row row = splitItem(table, names, line);
				// 加入集合
				cmd.add(row);
			} catch (IOException e) {
				throw new SyntaxException(e);
			}
		}
		
		return cmd;
	}
	
}