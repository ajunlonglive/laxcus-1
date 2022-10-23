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
import com.laxcus.access.function.table.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.select.*;
import com.laxcus.util.tip.*;

/**
 * SQL语法的<code>GROUP BY</code>和<code>HAVING</code>子句解析。<br>
 * 通过HAVING子句比较的必须是聚合函数。<br>
 * 
 * @author scott.liang
 * @version 1.3 12/3/2013
 * @since laxcus 1.0
 */
public class HavingParser extends GradationParser {

	/** LIKE 语句 **/
	private final static String SQL_FUNCTION_LIKE = "^\\s*(\\w+\\s*\\(\\s*\\w+\\s*\\))\\s+(?i)LIKE\\s+\\'([\\w\\W]+)\\'\\s*$";
	/** 字符串比较语句 **/
	private final static String SQL_FUNCTION_STRING = "^\\s*(\\w+\\s*\\(\\s*\\w+\\s*\\))\\s*(=|!=|<>)\\s*\\'([\\w\\W]+)\\'\\s*$";
	/** 数值类型语句 **/
	private final static String SQL_FUNCTION_NUMBER = "^\\s*(\\w+\\s*\\(\\s*\\w+\\s*\\))\\s*(=|!=|<>|>|<|>=|<=)\\s*([-]{0,1}[0-9]+[\\.]{0,1}[0-9]*)\\s*$";

	/**
	 * 构造一个默认的HAVING解析器
	 */
	public HavingParser() {
		super();
	}

	/**
	 * 根据正则表达式"贪婪"算法，找到"AND|OR"标记，分割字符串
	 * 
	 * @param input
	 * @return
	 */
	private String[] splitByLogic(String input) {
//		System.out.printf("{%s}\n", sql);
		// 贪婪算法，采取从右向左匹配(最大化匹配)
		final String regex = "^([\\w\\W]+)(\\s+(?i)AND|OR\\s+)([\\w\\W]+)\\s*$";
		List<String> array = new ArrayList<String>();
		do {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(input);
			if(!matcher.matches()) {
				array.add(0, input);
				break;
			}
			input = matcher.group(1);
			array.add(0, matcher.group(2) + matcher.group(3));
		} while(true);
		String[] s = new String[array.size()];
		return array.toArray(s);
	}
	
	/**
	 * 解析LIKE语句，返回列实例
	 * 
	 * @param columnId
	 * @param table
	 * @param input
	 * @return
	 */
	private RWord splitLike(short columnId, Table table, String input)  {
		ColumnAttribute attribute = table.find(columnId);
		if(attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, columnId);
		}
		LikeString string = splitLike(attribute, input);
		short left = string.getLeft();
		short right = string.getRight();
		String value = string.getValue();

		RWord column = null;
		try {
			if (attribute.isChar()) {
				column = VariableGenerator.createRChar((CharAttribute) attribute, left, right, value);
			} else if (attribute.isWChar()) {
				column = VariableGenerator.createRWChar((WCharAttribute) attribute, left, right, value);
			} else if (attribute.isHChar()) {
				column = VariableGenerator.createRHChar((HCharAttribute) attribute, left, right, value);
			}
		} catch (IOException e) {
			throw new SyntaxException(e);
		}
		return column;
	}

	/**
	 * 解析字符串，返回列实例
	 * 
	 * @param columnId
	 * @param table
	 * @param input
	 * @return
	 */
	private Column splitString(short columnId, Table table, String input) {
		ColumnAttribute attribute = table.find(columnId);
		if (attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, columnId);
		}
		if (!attribute.isWord()) {
			// throw new SyntaxException("this is not character: %d", columnId);
			throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, attribute.getName());
		}
		
		Column column = null;
		try {
			switch (attribute.getType()) {
			case ColumnType.CHAR:
				column = VariableGenerator.createChar(false, (CharAttribute) attribute, input);
				break;
			case ColumnType.WCHAR:
				column = VariableGenerator.createWChar(false, (WCharAttribute) attribute, input);
				break;
			case ColumnType.HCHAR:
				column = VariableGenerator.createHChar(false, (HCharAttribute) attribute, input);
				break;
			}
		} catch (IOException e) {
			throw new SyntaxException(e);
		}
		
		return column;
	}
	
	/**
	 * 解析参数，返回列实例
	 * 
	 * @param columnId
	 * @param table
	 * @param input
	 * @return
	 */
	private Column splitNumber(short columnId, Table table, String input) {
		ColumnAttribute attribute = table.find(columnId);
		// 如果没有列ID,默认返回一个整型值
		if(attribute == null) {
			return new com.laxcus.access.column.Integer((short)0, java.lang.Integer.parseInt(input) );
		}
		
		if (!attribute.isNumber()) {
//			throw new SyntaxException("this is not number: %d", columnId);
			throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, attribute.getNameText());
		}
		
		Column column = null;
		if (attribute.isShort()) {
			column = new com.laxcus.access.column.Short((short) 0, java.lang.Short.parseShort(input));
		} else if (attribute.isInteger()) {
			column = new com.laxcus.access.column.Integer((short) 0, java.lang.Integer.parseInt(input));
		} else if (attribute.isLong()) {
			column = new com.laxcus.access.column.Long((short) 0, java.lang.Long.parseLong(input));
		} else if (attribute.isFloat()) {
			column = new com.laxcus.access.column.Float((short) 0, java.lang.Float.parseFloat(input));
		} else if (attribute.isDouble()) {
			column = new com.laxcus.access.column.Double((short) 0, java.lang.Double.parseDouble(input));
		} else if (attribute.isDate()) {
			int value = CalendarGenerator.splitDate(input);
			column = new com.laxcus.access.column.Date((short) 0, value);
		} else if (attribute.isTime()) {
			int value = CalendarGenerator.splitTime(input);
			column = new com.laxcus.access.column.Time((short) 0, value);
		} else if (attribute.isTimestamp()) {
			long value = CalendarGenerator.splitTimestamp(input);
			column = new com.laxcus.access.column.Timestamp((short) 0, value);
		}
		
		return column;
	}
	
	/**
	 * 解析函数,返回查询
	 * 
	 * @param table
	 * @param input
	 * @return
	 */
	private Situation splitFunction(Table table, String input) {
		Pattern	pattern = Pattern.compile(HavingParser.SQL_FUNCTION_LIKE);
		Matcher matcher = pattern.matcher(input);
		if(matcher.matches()) {
			String funcText = matcher.group(1);
			String content = matcher.group(2);			
			// 处理转义字符
			content = content.replaceAll("\\'", "\'");

			ColumnFunction function = ColumnFunctionCreator.create(table, funcText);
			if (function == null) {
//				throw new SyntaxException("cannot create '%s'", funcText);
				throwableNo(FaultTip.SQL_ILLEGAL_FUNCTION_X, funcText);
			} else if(!(function instanceof ColumnAggregateFunction)) {
//				throw new SyntaxException("%s is not aggregate function", funcText);
				throwableNo(FaultTip.SQL_ILLEGAL_AGGREGATE_FUNCTION_X, funcText);
			}

			short columnId = ((ColumnAggregateFunction) function).getColumnId();
			RWord value = splitLike(columnId, table, content);
			
			return new Situation((ColumnAggregateFunction) function, CompareOperator.LIKE, value);
		}
		
		pattern = Pattern.compile(HavingParser.SQL_FUNCTION_STRING);
		matcher = pattern.matcher(input);
		if(matcher.matches()) {
			String funcText = matcher.group(1);
			String compare = matcher.group(2);
			String content = matcher.group(3);
			// 处理转义字符
			content = content.replaceAll("\\'", "\'");
			
			ColumnFunction function = ColumnFunctionCreator.create(table, funcText);
			if (function == null) {
				// throw new SyntaxException("cannot create '%s'", funcText);
				throwableNo(FaultTip.SQL_ILLEGAL_FUNCTION_X, funcText);
			} else if(!(function instanceof ColumnAggregateFunction)) {
//				throw new SyntaxException("%s is not aggregate function", funcText);
				throwableNo(FaultTip.SQL_ILLEGAL_AGGREGATE_FUNCTION_X, funcText);
			}
			
			short columnId = ((ColumnAggregateFunction) function).getColumnId();
			Column value = this.splitString(columnId, table, content);
			
			return new Situation((ColumnAggregateFunction)function, CompareOperator.translate(compare), value);
		}
		
		pattern = Pattern.compile(HavingParser.SQL_FUNCTION_NUMBER);
		matcher = pattern.matcher(input);
		if(matcher.matches()) {
			String funcText = matcher.group(1);
			String compare = matcher.group(2);
			String content = matcher.group(3);
			
			ColumnFunction function = ColumnFunctionCreator.create(table, funcText);
			if (function == null) {
				//				throw new SyntaxException("cannot create '%s'", funcText);
				throwableNo(FaultTip.SQL_ILLEGAL_FUNCTION_X, funcText);
			} else if(!(function instanceof ColumnAggregateFunction)) {
				//				throw new SyntaxException("%s is not aggregate function", funcText);
				throwableNo(FaultTip.SQL_ILLEGAL_AGGREGATE_FUNCTION_X, funcText);
			}
			
			short columnId = ((ColumnAggregateFunction) function).getColumnId();
			Column value = splitNumber(columnId, table, content);
			return new Situation((ColumnAggregateFunction)function, CompareOperator.translate(compare), value);
		}
		
//		throw new SyntaxException("Illegal: %s", input);
		throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
		return null;
	}
	
	/**
	 * 参数在进入前，已经取消了括号，语句之间可能会存在"AND|OR"
	 * 
	 * @param table
	 * @param sql
	 * @return
	 */
	private Situation splitUnit(Table table, String sql) {
		//1. 过滤两侧可能存在的无意义的括号
		sql = filteBrackets(sql);
		//2. 找到"AND|OR",分隔成多列查询条件
		String[] columns = splitByLogic(sql);

		//3.1  第1列是肯定不带(AND|OR)
		Situation situa = splitFunction(table, columns[0]);
		//3.2  从第2列或以后肯定要带(AND|OR)
		final String regex = "^\\s*(?i)(AND|OR)\\s+([\\w\\W]+)\\s*$";
		for(int i = 1; i < columns.length; i++) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(columns[i]);
			if (!matcher.matches()) {
				// throw new SyntaxException("error:%s", columns[i]);
				throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, columns[i]);
			}
			String logic = matcher.group(1);
			String query = matcher.group(2);
			
			Situation partner = splitFunction(table, query);
			partner.setRelation(LogicOperator.translate(logic));
			situa.addPartner(partner);
		}

		return situa;
	}

	/**
	 * 分割HAVING语句. 步骤:
	 * <1> 过滤最外两侧可能存在的括号(这个括号无用,但是必须匹配)
	 * <2> 按括号对进行分组
	 * <3> 检查每个一个分组,直到最小化(不存在括号)
	 * <4> 最小化后,放入单元中继续解析(splitUnit), 这一段设置"外部逻辑连接关系"
	 * <5> 解析单元首先将"AND|OR"提出来,对每一个列进行解析. 这一段设置同级逻辑连接关系)
	 * 
	 * @param table
	 * @param input
	 * @return
	 */
	private Situation splitSituation(Table table, String input) {
		//1. 过滤两侧的括号
		input = filteBrackets(input);
		//2. 按"对称的括号对"进行分组
		String[] groups = splitGroup(input);
		//3. 各分组检查
		Pattern pattern = Pattern.compile(HavingParser.SQL_PART_LOGICPREFIX);
		Situation parent = null;

		for(String group : groups) {			
			//3.1  如果开始存在逻辑连接符号,取出来
			byte relation = LogicOperator.NONE;
			Matcher matcher = pattern.matcher(group);
			if (matcher.matches()) {
				String logic = matcher.group(1);
				group = matcher.group(2);
				relation = LogicOperator.translate(logic);
			}
			
			//3.2  将一个分组切割成多个"段"
			String[] parts = splitGroup(group);
			
			//3.3  两种情况:<1>只有一个分组,表示没有括号,是最小单元. <2>继续分组
			if(parts.length == 1) {
				Situation situa = splitUnit(table, parts[0]);
				situa.setOuterRelation(relation);
				if(parent == null) parent = situa;
				else parent.attach(situa); // 下一级分组
			} else { // 有多组,继续分解,直以最小
				Situation slave = null;
				for(String part: parts) {
					byte slaveRelation = LogicOperator.NONE;
					matcher = pattern.matcher(part);
					if (matcher.matches()) {
						String logic = matcher.group(1);
						part = matcher.group(2);
						slaveRelation = LogicOperator.translate(logic);
					}
					
					Situation situa = splitSituation(table, part);
					situa.setOuterRelation(slaveRelation);
					if(slave == null) slave = situa;
					else slave.addPartner(situa);
				}
				slave.setOuterRelation(relation);
				if(parent == null) parent = slave;
				else parent.attach(slave);
			}
		}
		
		return parent;
	}

	/**
	 * 解析"HAVING"语句
	 * @param table 数据表
	 * @param input 输入语句
	 * @return 返回Situation
	 */
	public Situation split(Table table, String input) {
		return this.splitSituation(table, input);
	}
	
}