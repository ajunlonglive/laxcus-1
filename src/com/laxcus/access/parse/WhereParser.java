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
import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.select.*;
import com.laxcus.util.tip.*;

/**
 * SQL WHERE子句解析器。
 * 
 * @author scott.liang
 * @version 1.3 12/3/2011
 * @since laxcus 1.0
 */
public class WhereParser extends GradationParser {

	class LogicString {
		public String logic;
		public String value;

		public LogicString() {
			super();
		}

		public LogicString(String logicText, String valueText) {
			this.logic = logicText;
			this.value = valueText;
		}
	}

	/**
	 * BWTEEN有两种情况:
	 * <1> 前面如果有AND|OR逻辑符，再前面必须有一段其它字符
	 * <2> 前面如果没有AND|OR逻辑符，再前面必须是空的
	 * 
	 * 基本比较表达式: ([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\s+(?i)(?:NOT\s+BETWEEN|BETWEEN)\s+[\\w\\W]+?\s+(?i)AND\s+[\\w\\W]+?)\s*$
	 * 有其它参数: ^([\\w\\W]+)(?i)(\s+AND\s+|\s+OR\s+)(\w+\s+(?i)(?:NOT\s+BETWEEN|BETWEEN)\s+[\\w\\W]+?\s+(?i)AND\s+[\\w\\W]+?)\s*$
	 * 无参数: ^\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\s+(?i)(?:NOT\s+BETWEEN|BETWEEN)\s+[\\w\\W]+?\s+(?i)AND\s+[\\w\\W]+?)\s*$
	 * 
	 * ^(\s*|[\\w\\W]+\s+(?i)(AND\s+|OR\s+))(\w+\s+(?i)(?:NOT\s+BETWEEN|BETWEEN)\s+[\\w\\W]+?(?i)AND\s+[\\w\\W]+?)\s*$
	 * 
	 * 其它WHERE条件大致相同
	 */

	/******* SQL WHERE 子句格式 *********/
	private final static String SQL_WHERE_NULLEMPTY = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+(?i)(?:IS\\s+NULL|IS\\s+NOT\\s+NULL|IS\\s+EMPTY|IS\\s+NOT\\s+EMPTY))\\s*$";
	private final static String SQL_WHERE_BETWEEN = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+(?i)(?:NOT\\s+BETWEEN|BETWEEN)\\s+[\\w\\W]+?(?i)AND\\s+[\\w\\W]+?)\\s*$";
	private final static String SQL_WHERE_IN = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+(?i)(?:NOT\\s+IN|IN)\\s*\\([\\w\\W]+?\\))\\s*$";
	private final static String SQL_WHERE_DIGIT = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s*(?:=|!=|<>|>|<|>=|<=)\\s*(?:[+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]*))\\s*$";
	private final static String SQL_WHERE_RAW = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s*(?:=|!=|<>)\\s*(?i)(?:0X)(?:[0-9a-fA-F]+))\\s*$";
	private final static String SQL_WHERE_LIKE = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+(?i)(?:LIKE)\\s+\\'[\\w\\W]+?\\')\\s*$";
	private final static String SQL_WHERE_CALENDAR = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s*(?:=|!=|<>|>|<|>=|<=)\\s*\\'(?:[0-9\\.\\:\\-\\/\\p{Space}]+)\\')\\s*$";
	private final static String SQL_WHERE_STRING = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s*(?:=|!=|<>)\\s*\\'[\\w\\W]+?\\')\\s*$";

	/** SQL WHERE 嵌套检查语句 **/
	private final static String SQL_WHERE_SELECT_COMPARE = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s*(?:=|<>|!=|>|<|>=|<=)\\s*\\(\\s*(?i)(?:SELECT\\s+)[\\w\\W]+\\))\\s*$";
	private final static String SQL_WHERE_SELECT_IN = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+(?i)(?:IN|NOT\\s+IN)\\s*\\(\\s*(?i)(?:SELECT\\s+)[\\w\\W]+\\))\\s*$";
	private final static String SQL_WHERE_SELECT_EXISTS = "((?i)(?:EXISTS|NOT\\s+EXISTS)\\s+\\(\\s*(?i)(?:SELECT\\s+)[\\w\\W]+\\))\\s*$";
	private final static String SQL_WHERE_SELECT_ALLANY = "([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s*(?:=|<>|!=|>|<|>=|<=)\\s*(?i)(?:ALL|ANY|SOME)\\s*\\(\\s*(?i)(?:SELECT\\s+)[\\w\\W]+\\))\\s*$";

	/******** WHERE 各单元被检索值格式  **********/	
	private final static String SQL_COLUMN_ISNULL = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(IS\\s+NULL)\\s*$";
	private final static String SQL_COLUMN_NOTNULL = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(IS\\s+NOT\\s+NULL)\\s*$";

	private final static String SQL_COLUMN_ISEMPTY = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(IS\\s+EMPTY)\\s*$";
	private final static String SQL_COLUMN_NOTEMPTY = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(IS\\s+NOT\\s+EMPTY)\\s*$";

	private final static String SQL_COLUMN_RAW = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*(=|!=|<>)\\s*(?i)(?:0X)([0-9a-fA-F]+)\\s*$";
	private final static String SQL_COLUMN_LIKE = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(LIKE)\\s+\\'([\\w\\W]+)\\'\\s*$";
	private final static String SQL_COLUMN_NUMBER = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*(=|!=|<>|>|<|>=|<=)\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]*)\\s*$";
	private final static String SQL_COLUMN_STRING = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*(=|!=|<>)\\s*\\'([\\w\\W]+)\\'\\s*$";
	private final static String SQL_COLUMN_CALENDAR = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*(=|!=|<>|>|<|>=|<=)\\s*\\'([0-9\\.\\:\\-\\/\\p{Space}]+)\\'\\s*$";

	private final static String SQL_COLUMN_IN = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(NOT\\s+IN|IN)\\s*\\(([\\w\\W]+)\\)\\s*$";
	private final static String SQL_COLUMN_BETWEEN = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s+(?i)(BETWEEN|NOT\\s+BETWEEN)\\s*\\(\\s*([\\w\\W]+)\\s+(?i)AND\\s+([\\w\\W]+)\\)\\s*$";

	/** 嵌套检索的IN/NOT IN语句 **/
	private final static String SQL_COLUMN_SUBSELECT_IN = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*(?i)(IN|NOT\\s+IN)\\s*\\(\\s*((?i)(?:SELECT\\s+[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+FROM\\s+[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)[\\w\\W]+)\\)\\s*$";
	/** 嵌套检索的EXISTS/NOT EXISTS语句 **/
	private final static String SQL_COLUMN_SUBSELECT_EXISTS = "^\\s*(?i)(EXISTS|NOT\\s+EXISTS)\\s*\\(\\s*((?i)(?:SELECT\\s+[\\w\\W]+\\s+FROM\\s+[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)[\\w\\W]+)\\)\\s*$";
	/** 嵌套检索的SOME/ANY/ALL语句 **/
	private final static String SQL_COLUMN_SUBSELECT_ANYALL = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*(=|!=|<>|>|<|>=|<=)\\s*(?i)(ALL|ANY|SOME)\\s*\\(\\s*((?i)(?:SELECT\\s+[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+FROM\\s+[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)[\\w\\W]+)\\)\\s*$";
	/** 比较符号 **/
	private final static String SQL_COLUMN_SUBSELECT_COMPARE = "^\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*(=|!=|<>|>|<|>=|<=)\\s*\\(\\s*((?i)(?:SELECT\\s+[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\s+FROM\\s+[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+\\.[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)[\\w\\W]+)\\)\\s*$";
	
	/** WHERE COLUMN_NAME IN (...) 括号中的参数 */
	private final static String SQL_COLUMN_IN_STRING = "^\\s*\\'([\\w\\W]+?)\\'(\\s*|\\s*\\,\\s*\\'[\\w\\W]+)$";
	private final static String SQL_COLUMN_IN_CALENDAR = "^\\s*\\'([0-9\\.\\:\\-\\/\\p{Space}]+)\\'(\\s*|\\s*\\,\\s*\\'[\\w\\W]+)$";
	private final static String SQL_COLUMN_IN_NUMBER = "^\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]*)(\\s*|\\s*\\,[\\w\\W]+)$";
	private final static String SQL_COLUMN_IN_RAW = "^\\s*(?i)(?:0X)([0-9a-fA-F]+)(\\s*|\\s*\\,[\\w\\W]+)$";

	/** 判断前缀是逗号 **/
	private final static String FILTE_COMMA = "^\\s*(?:,)\\s*([\\w\\W]+)$";

	/**
	 * 构造默认的SQL WHERE语句解析器
	 */
	public WhereParser() {
		super();
	}

	/**
	 * 判断是不是逗号前缀
	 * 
	 * @param input
	 * @return
	 */
	private boolean isCommaPrefix(String input) {
		Pattern pattern = Pattern.compile(WhereParser.FILTE_COMMA);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 过滤逗号前缀(如果前面有逗号则过滤，否则原值返回)
	 * @param input
	 * @return
	 */
	private String filteCommaPrefix(String input) {
		Pattern pattern = Pattern.compile(WhereParser.FILTE_COMMA);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return input;
	}
	
	/**
	 * 右侧最小匹配，取出参数，分割条件是逻辑连接符号(AND|OR)<br>
	 * SQL语句最前面已经取消了逻辑符号(AND|OR)<br>
	 * 
	 * @param input
	 * @return
	 */
	private LogicString[] splitWhereMember(String input) {
		ArrayList<LogicString> array = new ArrayList<LogicString>();
		final String prefix = "^([\\w\\W]+\\s+(?i)(AND\\s+|OR\\s+)|\\s*)";
		while(input.trim().length() > 0) {
			//1. 检查 COLUMN_NAME [IS NULL|IS EMPTY|IS NOT NULL|IS NOT EMPTY...]
			Pattern pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_NULLEMPTY);
			Matcher matcher = pattern.matcher(input);
			boolean match = matcher.matches();
			//2. 检查 COLUMN_NAME [BETWEEN|NOT BETWEEN] ... AND ...
			if (!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_BETWEEN);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//3. 检查WHERE COLUMN_NAME IN ()
			if(!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_IN);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//4. 检查WHERE COLUMN_NAME (=|<>|!=|>=|<=|>|<) [digit]
			if(!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_DIGIT);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//5. 检查二进制数字
			if(!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_RAW);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//6. 检查WHERE COLUMN_NAME LIKE '...'
			if(!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_LIKE);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//7. 检查日期
			if(!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_CALENDAR);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//8. 检查字符 WHERE COLUMN_NAME [=|<>|!=] '...' 
			if(!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_STRING);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}

			//9. 嵌套检索: WHERE COLUMN_NAME =|<>|!=|>|<|>=|<= (SELECT ...)
			if (!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_SELECT_COMPARE);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//10. 嵌套检索: WHERE COLUMN_NAME IN|NOT IN (SELECT .... )
			if (!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_SELECT_IN);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//11. 嵌套检索 WHERE EXISTS|NOT EXISTS (SELECT .... )
			if (!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_SELECT_EXISTS);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}
			//12. 嵌套检索 WHERE COLUMN_NAME [=|<>|!=|>|<|>=|<=] [ALL|ANY|SOME]  (SELET .... )
			if (!match) {
				pattern = Pattern.compile(prefix + WhereParser.SQL_WHERE_SELECT_ALLANY);
				matcher = pattern.matcher(input);
				match = matcher.matches();
			}

			if (!match || matcher.groupCount() != 3) {
//				throw new SyntaxException("illegal syntax:%s", input);
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
			}

			// 解析字符串，保存在数组集合的最前面
			input = matcher.group(1);
			String logic = matcher.group(2);
			if (logic == null) logic = "";
			input = input.substring(0, input.length() - logic.length());
			array.add(0, new LogicString(logic, matcher.group(3)));
		}

		LogicString[] s = new LogicString[array.size()];
		return array.toArray(s);
	}

	/**
	 * 处理字符串中的转义字符
	 * 
	 * @param s
	 * @return
	 */
	private String translate(String s) {
		return s.replaceAll("\\'", "\'");
	}

	/**
	 * 判断IS NULL|IS NOT NULL，生成条件
	 * @param table
	 * @param name
	 * @param isnull
	 * @return
	 */
	private Where splitNull(Table table, String name, boolean isnull) {
		ColumnAttribute attribute = table.find(name);
		if(attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}

		ColumnIndex index = null;
		try {
			index = IndexGenerator.createNullIndex(attribute, isnull);
		} catch (IOException e) {
			throw new SyntaxException(e);
		}

		return new Where((isnull ? CompareOperator.IS_NULL : CompareOperator.NOT_NULL), index);
	}

	/**
	 * 判断IS EMPTY|IS NOT EMPTY，生成检索条件(只限可变长类型)<br>
	 * 
	 * @param table
	 * @param name
	 * @param isempty
	 * @return
	 */
	private Where splitEmpty(Table table, String name, boolean isempty) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}

		// 只限可变长类型
		Column column = null;
		try {
			if (attribute.isRaw()) {
				column = VariableGenerator.createRaw(table.isDSM(), (RawAttribute)attribute, new byte[0]);
			} else if(attribute.isWord()) {
				column = VariableGenerator.createWord(table.isDSM(), (WordAttribute) attribute, new String());
			} else {
				throwableNo(FaultTip.NOTSUPPORT_X, name);
			}
		} catch(IOException e) {
			throw new SyntaxException(e);
		}

		column.setId(attribute.getColumnId());
		column.setNull(false);

		LongIndex index = new LongIndex(0, column);

		return new Where((isempty ? CompareOperator.IS_EMPTY : CompareOperator.NOT_EMPTY), index);
	}

	/**
	 * 解析 WHERE column_name [NOT IN|IN] (value1, value2...) 语句
	 * @param table
	 * @param name
	 * @param isIn
	 * @param input
	 * @return
	 */
	private Where splitIn(Table table, String name, boolean isIn, String input) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}

		final String operator = (isIn ? "=" : "<>");
		final byte relate = (isIn ? LogicOperator.OR : LogicOperator.AND);

		Where parent = null;
		for (int index = 0; input.trim().length() > 0; index++) {
			//1. 必须有逗号做分隔符
			if(index > 0) {
				if(!isCommaPrefix(input)) {
//					throw new SyntaxException("illegal:%s", input);
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
				}
				input = filteCommaPrefix(input);
			}

			if (attribute.isWord()) {
				// 去掉两侧的引号
				Pattern pattern = Pattern.compile(WhereParser.SQL_COLUMN_IN_STRING);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
//					throw new SyntaxException("illegal string: %s", input);
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
				}
				String value = matcher.group(1);
				input = matcher.group(2);

				// 字符串转义
				value = translate(value);
				Where condi = splitString(table, name, operator, value);
				if (parent == null) {
					parent = condi;
				} else {
					condi.setRelation(relate);
					parent.addPartner(condi);
				}
			} else if (attribute.isCalendar()) {
				Pattern pattern = Pattern.compile(WhereParser.SQL_COLUMN_IN_CALENDAR);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
//					throw new SyntaxException("illegal calendar:%s", input);
					throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
				}
				String value = matcher.group(1);
				input = matcher.group(2);

				Where condi = this.splitString(table, name, operator, value);
				if(parent == null) {
					parent = condi;
				} else {
					condi.setRelation(relate);
					parent.addPartner(condi);
				}
			} else if (attribute.isNumber()) {
				Pattern pattern = Pattern.compile(WhereParser.SQL_COLUMN_IN_NUMBER);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
//					throw new SyntaxException("illegal number:%s", input);
					throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
				}
				String value = matcher.group(1);
				input = matcher.group(2);

				Where condi = splitNumber(table, name, operator, value);
				if(parent == null) {
					parent = condi;
				} else {
					condi.setRelation(relate);
					parent.addPartner(condi);
				}
			} else if (attribute.isRaw()) {
				Pattern pattern = Pattern.compile(WhereParser.SQL_COLUMN_IN_RAW);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
//					throw new SyntaxException("illegal binary:%s", input);
					throwableNo(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
				}
				String value = matcher.group(1);
				input = matcher.group(2);

				Where condi = this.splitRaw(table, name, operator, value);
				if(parent == null) {
					parent = condi;
				} else {
					condi.setRelation(relate);
					parent.addPartner(condi);
				}
			}
		}

		return parent;		
	}

	/**
	 * 解析 WHERE column_name [BETWEEN|NOT BETWEEN] value1 AND value2 语句。<br>
	 * 不支持可变长数据类型(字符串或者二进制数组)。<br>
	 * 
	 * @param table
	 * @param name
	 * @param isBetween
	 * @param value1
	 * @param value2
	 * @return
	 */
	private Where splitBetween(Table table, String name, boolean isBetween, String value1, String value2) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}
		// 不支持可变长类型(二进制字节和字符)
		if (attribute.isVariable()) {
//			throw new SyntaxException("could not support variable by %s", name);
			throwableNo(FaultTip.SQL_CANNOTSUPPORT_X, name);
		}

		String compare1 = (isBetween ? ">=" : "<");
		String compare2 = (isBetween ? "<=" : ">");
		byte relate = (isBetween ? LogicOperator.AND : LogicOperator.OR);

		// 如果是日期/时间格式，过滤掉两侧引号
		if(attribute.isCalendar()) {
			final String regex = "^\\s*\\'([\\w\\W]+)\\'\\s*$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(value1);
			if (!matcher.matches()) {
//				throw new SyntaxException("illegal calendar style:%s", value1);
				throwableNo(FaultTip.SQL_ILLEGAL_STYLE_X, value1);
			}
			value1 = matcher.group(1);
			matcher = pattern.matcher(value2);
			if (!matcher.matches()) {
//				throw new SyntaxException("illegal calendar style:%s", value2);
				throwableNo(FaultTip.SQL_ILLEGAL_STYLE_X, value2);
			}
			value2 = matcher.group(1);
		}
		// 解析参数
		if (attribute.isCalendar()) {
			Where condi1 = this.splitString(table, name, compare1, value1);
			Where condi2 = this.splitString(table, name, compare2, value2);
			condi2.setRelation(relate);
			condi1.addPartner(condi2);
			return condi1;
		} else if (attribute.isNumber()) {
			Where condi1 = this.splitNumber(table, name, compare1, value1);
			Where condi2 = this.splitNumber(table, name, compare2, value2);
			condi2.setRelation(relate);
			condi1.addPartner(condi2);
			return condi1;
		}

		throwableNo(FaultTip.SQL_ILLEGAL_ATTRIBUTE_X, name);
		return null;
//		throw new SyntaxException("invalid attribute: %s", name);
	}

	/**
	 * 解析二地制数据，生成检索条件
	 * 
	 * @param table
	 * @param name
	 * @param compare
	 * @param value
	 * @return
	 */
	private Where splitRaw(Table table, String name, String compare, String value) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}
		if (!attribute.isRaw()) {
			// throwable("invalid column:%s", name);
			throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, name);
		}
		
		// 比较符
		byte symbol = CompareOperator.translate(compare);
		if (!CompareOperator.isRawOperator(symbol)) {
			throwableNo(FaultTip.ILLEGAL_WHERE_OPERATOR, name, compare);
		}

		// 生成二进制数据索引
		try {			
			ColumnIndex index = IndexGenerator.createRawIndex(table.isDSM(), (RawAttribute) attribute, value);
			return new Where(symbol, index);
		} catch (IOException e) {
			throw new SyntaxException(e);
		}
	}

	/**
	 * 生成LIKE比较条件，只限字符类型
	 * 
	 * @param table
	 * @param name
	 * @param value
	 * @return
	 */
	private Where splitLike(Table table, String name, String value) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.SQL_CANNOTSUPPORT_X, name);
		}
		if (!attribute.isWord()) {
			throwableNo(FaultTip.SQL_CANNOTSUPPORT_X, name);
//			throw new SyntaxException("could not support:%s", name);
		}

		LikeString string = super.splitLike(attribute, value);
		short left = string.getLeft();
		short right = string.getRight();
		String text = string.getValue();

		try {
			ColumnIndex index = IndexGenerator.createLikeIndex(table.isDSM(),
					(WordAttribute) attribute, left, right, text);
			return new Where(CompareOperator.LIKE, index);
		} catch (IOException e) {
			throw new SyntaxException(e);
		}
	}

	/**
	 * 解析SELECT嵌套(IN|NOT IN)查询
	 * @param table
	 * @param name
	 * @param in
	 * @param input SELECT语句
	 * @return 返回Where实例
	 */
	private Where splitSubSelectByIn(Table table, String name, boolean in, String input, boolean online) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}

		// 解析 "SELECT ... FROM schema.table" 语句
		SelectParser parser = new SelectParser();
		Select select = parser.split(input, online);
		// 检查: <1>显示列只能一个，<2>列属性一致
		ListSheet sheet = select.getListSheet();
		if (sheet.size() != 1) {
			throwableNo(FaultTip.NOTSUPPORT_X, input);
		}
		ListElement element = sheet.get(0);
		// 必须是列属性
		if(!element.isColumn()) {
//			throw new SyntaxException("illegal column: %s", input);
			throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
		}
		if (attribute.getType() != element.getFamily()) {
//			throw new SyntaxException("not match family!");
			throwableNo(FaultTip.SQL_TYPE_NOTMATCH_X, name); 
		}
		// 嵌套不支持"GROUP BY"和"ORDER BY"语句
		if (select.getGroup() != null || select.getOrder() != null) {
			throwableNo(FaultTip.NOTSUPPORT_X, input);
//			throw new SyntaxException("cannot support %s", input);
		}

		NestedIndex index = new NestedIndex(attribute.getColumnId(), select);
		return new Where((in ? CompareOperator.IN : CompareOperator.NOT_IN), index);
	}

	/**
	 * 解析嵌套SELECT (=|<>|>|<|>=|<=) (ANY|ALL)语句
	 * @param table
	 * @param name
	 * @param operator
	 * @param input
	 * @return
	 */
	private Where splitSubSelectByAnyAll(Table table, String name, byte operator, String input, boolean online) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.SQL_ILLEGAL_ATTRIBUTE_X, name); // 没有找到对应的列属性
		}

		// 解析嵌套查询
		SelectParser parser = new SelectParser();
		Select select = parser.split(input, online);
		// 检查: <1>显示列只能一个，<2>列属性一致
		ListSheet sheet = select.getListSheet();
		if (sheet.size() != 1) {
			throwableNo(FaultTip.SQL_CANNOTSUPPORT_X, input);
		}
		ListElement element = sheet.get(0);
		// 必须是列属性
		if (!element.isColumn()) {
			throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, name);
		}
		
		if (attribute.getType() != element.getFamily()) {
			String str = String.format("%s/%s", name, element.getName());
			throwableNo(FaultTip.SQL_TYPE_NOTMATCH_X, str);
			// throwable(FaultTip.SQL_TYPE_NOTMATCH_X, name);
		}

		NestedIndex index = new NestedIndex(attribute.getColumnId(), select);
		return new Where(operator, index);
	}

	/**
	 * 解析嵌套的EXISTS|NOT EXISTS语句
	 * @param table
	 * @param exists
	 * @param input
	 * @return
	 */
	private Where splitSubSelectByExists(Table table, boolean exists, String input, boolean online) {
		// 解析嵌套语句
		SelectParser parser = new SelectParser();
		Select select = parser.split(input, online);
		// 子检索索引
		NestedIndex index = new NestedIndex();
		index.setSelect(select);
		// 返回检索条件
		return new Where((exists ? CompareOperator.EXISTS : CompareOperator.NOT_EXISTS), index);
	}

	/**
	 * 解析字符串格式
	 * 
	 * @param table
	 * @param name
	 * @param compare
	 * @param text
	 * @return
	 */
	private Where splitString(Table table, String name, String compare, String text) {
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}

		ColumnIndex index = null;
		if (attribute.isWord()) {
			try {
				index = IndexGenerator.createWordIndex(table.isDSM(), (WordAttribute) attribute, text);
			} catch (IOException e) {
				throw new SyntaxException(e);
			}
		} else if (attribute.isCalendar()) {
			if(attribute.isDate()) {
				int num = CalendarGenerator.splitDate(text);
				index = IndexGenerator.createDateIndex(num, attribute);
			} else if (attribute.isTime()) {
				int num = CalendarGenerator.splitTime(text);
				index = IndexGenerator.createTimeIndex(num, attribute);
			} else if (attribute.isTimestamp()) {
				long num = CalendarGenerator.splitTimestamp(text);
				index = IndexGenerator.createTimestampIndex(num, attribute);
			}
		} else {
//			throw new SyntaxException("illegal attribute: %s - %s", name, text);
			throwableNo(FaultTip.SQL_ILLEGAL_ATTRIBUTE_X, name);
		}

		// 生成条件
		byte symbol = CompareOperator.translate(compare);
		
		if (attribute.isWord()) {
			// 判断操作符无效
			if (!CompareOperator.isWordOperator(symbol)) {
				throwableNo(FaultTip.ILLEGAL_WHERE_OPERATOR, name, compare);
			}
		} else if (attribute.isCalendar()) {
			if(!CompareOperator.isCalindaOperator(symbol)) {
				throwableNo(FaultTip.ILLEGAL_WHERE_OPERATOR, name, compare);
			}
		}
		
		return new Where(symbol, index);
	}

	/**
	 * 解析定长参数（日期/时间、数值），生成比较条件
	 * @param table
	 * @param name
	 * @param compare
	 * @param value
	 * @return
	 */
	private Where splitNumber(Table table, String name, String compare, String value) {
		ColumnAttribute attribute = table.find(name);
		if(attribute == null) {
			throwableNo(FaultTip.NOTFOUND_X, name);
		}
		if (!attribute.isNumber()) {
			throwableNo(FaultTip.NOTMATCH_X, String.format("%s - %s", name, value));
		}

		ColumnIndex index = null;
		if (attribute.isShort()) {
			short num = NumberGenerator.splitShort(value); 
			index = IndexGenerator.createShortIndex(num, attribute);
		} else if (attribute.isInteger()) {
			int num = NumberGenerator.splitInt(value); 
			index = IndexGenerator.createIntegerIndex(num, attribute);
		} else if (attribute.isLong()) {
			long num = NumberGenerator.splitLong(value); 
			index = IndexGenerator.createLongIndex(num, attribute);
		} else if (attribute.isFloat()) {
			float num = NumberGenerator.splitFloat(value); 
			index = IndexGenerator.createFloatIndex(num, attribute);
		} else if (attribute.isDouble()) {
			double num = NumberGenerator.splitDouble(value); 
			index = IndexGenerator.createDoubleIndex(num, attribute);
		}

		// 操作符
		byte symbol = CompareOperator.translate(compare);
		if (!CompareOperator.isNumberOperator(symbol)) {
			throwableNo(FaultTip.ILLEGAL_WHERE_OPERATOR, name, compare);
		}

		Where condi = new Where(symbol, index);
		return condi;	
	}

	/**
	 * 解析一列，返回查询条件<br>
	 * 查询条件: <br>
	 * <1>IS NULL <2>NOT NULL <3>EMPTY <4>NOT EMPTY <5>SELECT嵌套 <6>字符串  <7>数值类型<br>
	 * 
	 * @param table 表实例
	 * @param input 输入语句
	 * @return 返回解析的Where语句
	 */
	private Where splitColumn(Table table, String input, boolean online) {
		// 1. 空值检查
		Pattern pattern = Pattern.compile(WhereParser.SQL_COLUMN_ISNULL);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			return splitNull(table, name, true);
		}
		// 2. 非空值检查
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_NOTNULL);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			return splitNull(table, name, false);
		}
		// 3. EMPTY值检查(限可变长类型, RAW,CHAR,WCHAR,HCHAR)
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_ISEMPTY);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			return splitEmpty(table, name, true);
		}
		// 4. 非EMPTY值检查
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_NOTEMPTY);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			return splitEmpty(table, name, false);
		}
		// 5. 嵌套SELECT的IN语句
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_SUBSELECT_IN);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String compare = matcher.group(2);
			String syntax = matcher.group(3);
			boolean in = compare.matches("^\\s*(?i)IN\\s*$");
			return splitSubSelectByIn(table, name, in, syntax, online);
		}
		// 6. 嵌套SELECT的EXISTS|NOT EXISTS语句
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_SUBSELECT_EXISTS);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String operator = matcher.group(1);
			String syntax = matcher.group(2);
			boolean exists = operator.matches("^\\s*(?i)EXISTS\\s*$");
			return splitSubSelectByExists(table, exists, syntax, online);
		}
		// 7. 嵌套SELECT的ANY/ALL语句
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_SUBSELECT_ANYALL);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String compare = matcher.group(2);
			String symbol = matcher.group(3);
			String syntax = matcher.group(4);
			byte operator = CompareOperator.translate(compare + symbol);
			return splitSubSelectByAnyAll(table, name, operator, syntax, online);
		}
		// 8. 嵌套SELECT的比较符语句 ID >,>=,<,<=,<> ...
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_SUBSELECT_COMPARE);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String compare = matcher.group(2);
			String syntax = matcher.group(3);
			byte operator = CompareOperator.translate(compare);
			return splitSubSelectByAnyAll(table, name, operator, syntax, online);
		}
		
		// 9. 检查IN关键字
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_IN);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String operator = matcher.group(2); // NOT IN | IN
			String value = matcher.group(3);
			boolean in = operator.matches("^\\s*(?i)IN\\s*$");
			return splitIn(table, name, in, value);
		}
		// 10. 检查BETWEEN关键字和值
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_BETWEEN);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String operator = matcher.group(2);
			String value1 = matcher.group(3);
			String value2 = matcher.group(4);
			boolean yes = operator.matches("^\\s*(?i)BETWEEN\\s*$"); // "BETWEEN".equalsIgnoreCase(abole);
			return splitBetween(table, name, yes, value1, value2);
		}
		// 11. 二进制数据搜索(只限RAW类型)
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_RAW);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String compare = matcher.group(2);
			String value = matcher.group(3);
			return splitRaw(table, name, compare, value);
		}
		// 12. 字符串LIKE查询(限CHAR,WCHAR,HCHAR)
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_LIKE);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			// 在解析前处理转义字符
			return splitLike(table, name, translate(value));
		}
		// 13. 数值类型查询
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_NUMBER);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String compare = matcher.group(2);
			String value = matcher.group(3);
			return splitNumber(table, name, compare, value);
		}
		// 14. 日期格式
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_CALENDAR);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String compare = matcher.group(2);
			String value = matcher.group(3);
			return splitString(table, name, compare, value);
		}
		// 15. 字符串查询
		pattern = Pattern.compile(WhereParser.SQL_COLUMN_STRING);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String compare = matcher.group(2);
			String value = matcher.group(3);
			// 解析前处理转义字符
			return splitString(table, name, compare, translate(value));
		}

		// 弹出异常
		String result = fault(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
		throw new SyntaxException(result);

		//		throwable(FaultTip.SQL_INCORRECT_SYNTAX_X, input);
		//		return null;
		//		throw new SyntaxException("illegal: %s", input);
	}

	/**
	 * 参数在进入前，已经取消了括号，语句之间可能会存在"AND|OR"
	 * @param table
	 * @param input
	 * @return
	 */
	private Where splitUnit(Table table, String input, boolean online) {
		// 1. 过滤两侧可能存在的无意义的括号
		input = filteBrackets(input);
		// 2. 根据逻辑连接符(AND|OR)，分隔成多列查询条件
		LogicString[] columns = this.splitWhereMember(input);

		// 3.1 第1列是肯定不带(AND|OR)
		Where basic = splitColumn(table, columns[0].value, online);
		// 3.2 从第2列或以后肯定要带(AND|OR)
		for (int i = 1; i < columns.length; i++) {
			String logic = columns[i].logic;
			String query = columns[i].value;

			Where partner = splitColumn(table, query, online);
			if (logic != null && logic.length() > 0) {
				partner.setRelation(LogicOperator.translate(logic));
			}
			basic.addPartner(partner);
		}

		return basic;
	}

	/**
	 * 分割WHERE语句. 步骤:
	 * <1> 过滤最外两侧可能存在的括号(这个括号无用,但是必须匹配)
	 * <2> 按括号对进行分组
	 * <3> 检查每个一个分组,直到最小化(不存在括号)
	 * <4> 最小化后,放入单元中继续解析(splitUnit), 这一段设置"外部逻辑连接关系"
	 * <5> 解析单元首先将"AND|OR"提出来,对每一个列进行解析. 这一段设置同级逻辑连接关系)
	 * 
	 * @param table
	 * @param sqlWhere
	 */
	private Where splitCondition(Table table, String sqlWhere, boolean online) {
		// 1. 过滤两侧的括号
		sqlWhere = filteBrackets(sqlWhere);
		// 2. 按"对称的括号对"进行分组
		String[] groups = splitGroup(sqlWhere);
		// 3. 各分组检查
		Pattern pattern = Pattern.compile(GradationParser.SQL_PART_LOGICPREFIX);
		Where parent = null;

		for (String group : groups) {
			// 3.1 如果开始存在逻辑连接符号,取出来
			byte relation = LogicOperator.NONE;
			Matcher matcher = pattern.matcher(group);
			if (matcher.matches()) {
				String logic = matcher.group(1);
				group = matcher.group(2);
				relation = LogicOperator.translate(logic);
			}
			// 3.2 将一个分组切割成多个"段"
			String[] parts = splitGroup(group);

			// 3.3 两种情况:<1>只有一个分组,表示没有括号,是最小单元. <2>继续分组
			if (parts.length == 1) {
				Where condi = splitUnit(table, parts[0], online);
				condi.setOuterRelation(relation);
				if(parent == null) parent = condi;
				else parent.attach(condi); // 下一级分组。绑定到最后，无论前面有多少个检索条件
			} else { // 有多组,继续分解,直以最小
				Where slave = null;
				for(String part: parts) {
					byte slaveRelation = LogicOperator.NONE;
					matcher = pattern.matcher(part);
					if (matcher.matches()) {
						String logic = matcher.group(1);
						part = matcher.group(2);
						slaveRelation = LogicOperator.translate(logic);
					}

					Where condi = splitCondition(table, part, online);
					condi.setOuterRelation(slaveRelation);
					if(slave == null) slave = condi;
					else slave.addPartner(condi);
				}
				slave.setOuterRelation(relation);
				if(parent == null) parent = slave;
				else parent.attach(slave); // 绑定到最后，无论前面有多少个检索条件
			}
		}

		return parent;
	}

	/**
	 * 解析"WHERE"语句
	 * @param table 表
	 * @param input 输入语句
	 * @return 返回Where命令
	 */
	public Where split(Table table, String input, boolean online) {
		return splitCondition(table, input, online);
	}
}