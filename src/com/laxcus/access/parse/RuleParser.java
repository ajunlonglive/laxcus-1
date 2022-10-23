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
import com.laxcus.access.schema.*;
import com.laxcus.access.util.*;
import com.laxcus.law.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.tip.*;

/**
 * 事务规则解析器
 * 
 * 事务语句格式：<br>
 * 1. ATTACH ALL BE SHARE|NOT SHARE [AND ATTACH ...] <br>
 * 2. ATTACH DATABASE 数据库 BE SHARE|NOT SHARE [AND ATTACH ...] <br>
 * 3. ATTACH TABLE 数据库.表 BE SHARE|NOT SHARE [AND ATTACH ...] <br>
 * 4. ATTACH ROW 数据库.表/列 列参数 BE SHARE|NOT SHARE [AND ATTACH ...] <br>
 * 
 * @author scott.liang
 * @version 1.0 8/9/2012
 * @since laxcus 1.0
 */
public class RuleParser extends SyntaxParser {

	/** 系统的事务规则 **/
	private final static String RULE_PREFIX = "^\\s*(?i)(?:AND)\\s+(?i)(ATTACH\\s+[\\p{ASCII}\\W]+)$";
	private final static String RULE_ALL = "^\\s*(?i)(?:ATTACH\\s+ALL\\s+BE)\\s+(?i)(SHARE|NOT\\s+SHARE)(?i)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String RULE_DATABASE = "^\\s*(?i)(?:ATTACH\\s+DATABASE)\\s+([\\p{ASCII}\\W]+?)\\s+(?i)(?:BE)\\s+(?i)(SHARE|NOT\\s+SHARE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String RULE_TABLE = "^\\s*(?i)(?:ATTACH\\s+TABLE)\\s+([\\p{ASCII}\\W]+?)\\s+(?i)(?:BE)\\s+(?i)(SHARE|NOT\\s+SHARE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	private final static String RULE_ROW = "^\\s*(?i)(?:ATTACH\\s+ROW)\\s+([\\p{ASCII}\\W]+?)\\s+(?i)(?:BE)\\s+(?i)(SHARE|NOT\\s+SHARE)(\\s*|\\s+[\\p{ASCII}\\W]+)$";
	
	/** 行规则格式 **/
	private final static String FEATURE = "^\\s*([\\w\\W]+)(?:[\\s+|\\/])([\\w\\W]+)\\s+([\\w\\W]+)\\s*$";
	/** 二进制数字 **/
	private final static String RAW = "^\\s*(?i)0x([0-9a-fA-F]+)\\s*$";
	/** 字符串/日期文字 **/
	private final static String STRING = "^\\s*\\'([\\w\\W]+)\\'\\s*$";

	/**
	 * 构造默认的事务规则解析器
	 */
	protected RuleParser() {
		super();
	}

	/**
	 * 拆解数据库名称
	 * @param input 输入语句
	 * @return 数据库名列表
	 */
	private List<Fame> splitRuleSchemas(String input, boolean online) {
		// 分割逗号
		String[] items = splitCommaSymbol(input);
		ArrayList<Fame> array = new ArrayList<Fame>();
		for (int i = 0; i < items.length; i++) {
			Fame fame = new Fame(items[i].trim());
			if (online) {
				if (!hasSchema(fame)) {
					throwableNo(FaultTip.NOTFOUND_X, fame);
				}
			}
			array.add(fame);
		}
		return array;
	}
	
	/**
	 * 解析列
	 * @param dsm 行存储格式
	 * @param attribute 列属性
	 * @param input 输入文本
	 * @return 返回列值
	 * @throws IOException - 弹出IO异常
	 */
	private Column splitColumn(boolean dsm, ColumnAttribute attribute, String input) throws IOException {
		if (attribute.isRaw()) {
			// 二进制格式
			Pattern pattern = Pattern.compile(RuleParser.RAW);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.matches()) {
				//				throw new SyntaxException("illegal raw:%s", input);
				throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, input);
			}
			input = matcher.group(1);
			return VariableGenerator.createRaw(dsm, (RawAttribute) attribute, input);
		} else if (attribute.isCalendar() || attribute.isWord()) {
			// 字符串格式，按照字符串格式分解
			Pattern pattern = Pattern.compile(RuleParser.STRING);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.matches()) {
				// throw new SyntaxException("illegal string:%s", input);
				throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, input);
			}
			input = matcher.group(1);

			if (attribute.isChar()) {
				return VariableGenerator.createChar(dsm, (CharAttribute) attribute, input);
			} else if (attribute.isWChar()) {
				return VariableGenerator.createWChar(dsm, (WCharAttribute) attribute, input);
			} else if (attribute.isHChar()) {
				return VariableGenerator.createHChar(dsm, (HCharAttribute) attribute, input);
			} else if (attribute.isDate()) {
				return CalendarGenerator.createDate((DateAttribute) attribute, input);
			} else if (attribute.isTime()) {
				return CalendarGenerator.createTime((TimeAttribute) attribute, input);
			} else if (attribute.isTimestamp()) {
				return CalendarGenerator.createTimestamp((TimestampAttribute) attribute, input);
			}
		} else if (attribute.isNumber()) {
			if (attribute.isShort()) {
				return NumberGenerator.createShort((ShortAttribute) attribute, input);
			} else if (attribute.isInteger()) {
				return NumberGenerator.createInteger((IntegerAttribute)attribute, input);
			} else if (attribute.isLong()) {
				return NumberGenerator.createLong((LongAttribute)attribute, input);
			} else if (attribute.isFloat()) {
				return NumberGenerator.createFloat((FloatAttribute) attribute, input);
			} else if (attribute.isDouble()) {
				return NumberGenerator.createDouble((DoubleAttribute) attribute, input);
			}
		}
		// 返回空指针
		return null;
	}
	
	/**
	 * 解析行规则
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回行特征符
	 */
	private List<RowFeature> splitRuleRows(String input, boolean online) {
		String[] items = splitCommaSymbol(input);
		ArrayList<RowFeature> array = new ArrayList<RowFeature>();
		for (String item : items) {
			Pattern pattern = Pattern.compile(RuleParser.FEATURE);
			Matcher matcher = pattern.matcher(item);
			if (!matcher.matches()) {
				throwable(message(FaultTip.INCORRECT_SYNTAX_X, item));
			}

			String prefix = matcher.group(1);
			String suffix = matcher.group(2);
			String param = matcher.group(3);
			
			// 判断是正确的表名
			if (!Space.validate(prefix)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
			}
			Space space = new Space(prefix);
			// 不存在，弹出错误
			if (online) {
				if (!hasTable(space)) {
					throwableNo(FaultTip.NOTFOUND_X, space);
				}
			}
			// 查找表
			Table table = findTable(space);
			if (table == null) {
				throwableNo(FaultTip.NOTFOUND_X, space);
			}
			ColumnAttribute attribute = table.find(suffix);
			if (attribute == null) {
				throwableNo(FaultTip.NOTFOUND_X, suffix);
			}

			// 解析列参数
			Column column = null;
			try {
				column = splitColumn(table.isDSM(), attribute, param);
			} catch (IOException e) {
				throw new SyntaxException(e);
			}
			if (column == null) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
			}

			// 保存一行标记
			Dock dock = new Dock(space, attribute.getColumnId());
			RowFeature feature = new RowFeature(dock, column);
			array.add(feature);
		}

		return array;
	}
	
	/**
	 * 拆解事务操作符
	 * @param input 输入语句
	 * @return 返回事务操作符
	 */
	private byte splitRuleOperator(String input) {
		byte operator = -1;
		if (input.matches("^\\s*(?i)(SHARE)\\s*$")) {
			operator = RuleOperator.SHARE_READ;
		} else if (input.matches("^\\s*(?i)(NOT\\s+SHARE)\\s*$")) {
			operator = RuleOperator.EXCLUSIVE_WRITE;
		}
		if (!RuleOperator.isOperator(operator)) {
			throwable(message(FaultTip.INCORRECT_SYNTAX_X, input));
		}
		return operator;
	}

	/**
	 * 解析事务规则
	 * @param input 输入的事务规则语句
	 * @param online 在线模式
	 * @return 返回解析的事务列表
	 */
	protected List<RuleItem> splitRules(String input, boolean online) {	
		ArrayList<RuleItem> array = new ArrayList<RuleItem>();
		for (int i = 0; input.trim().length() > 0; i++) {
			// 过滤前缀“AND”字符
			if (i > 0) {
				Pattern pattern = Pattern.compile(RuleParser.RULE_PREFIX);
				Matcher matcher = pattern.matcher(input);
				if (!matcher.matches()) {
					throwable(message(FaultTip.INCORRECT_SYNTAX_X, input));
				}
				input = matcher.group(1);
			}

			// 事务判断
			Pattern pattern = Pattern.compile(RuleParser.RULE_ALL);
			Matcher matcher = pattern.matcher(input);
			// 用户级事务
			if (matcher.matches()) {
				byte operator = splitRuleOperator(matcher.group(1));
				// 生成事务规则
				UserRuleItem rule = new UserRuleItem(operator);
				array.add(rule); // 保存
				input = matcher.group(2); // 后续参数
				continue;
			}
			// 数据库级事务
			pattern = Pattern.compile(RuleParser.RULE_DATABASE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				// 数据库名
				List<Fame> fames = splitRuleSchemas(matcher.group(1), online);
				// 操作符
				byte operator = splitRuleOperator(matcher.group(2));
				// 数据库事务规则
				for (Fame fame : fames) {
					SchemaRuleItem rule = new SchemaRuleItem(operator, fame);
					array.add(rule);
				}
				// 其它参数
				input = matcher.group(3);
				continue;
			}
			// 表级事务
			pattern = Pattern.compile(RuleParser.RULE_TABLE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				// 表名和操作符
				List<Space> spaces = splitSpaces(matcher.group(1), online);
				byte operator = splitRuleOperator(matcher.group(2));
				// 表事务
				for(Space space : spaces) {
					TableRuleItem rule = new TableRuleItem(operator, space);
					array.add(rule);
				}
				// 其它参数
				input = matcher.group(3);
				continue;
			}
			// 行级事务
			pattern = Pattern.compile(RuleParser.RULE_ROW);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				List<RowFeature> features = splitRuleRows(matcher.group(1), online);
				byte operator = splitRuleOperator(matcher.group(2));
				for (RowFeature feature : features) {
					RowRuleItem rule = new RowRuleItem(operator, feature);
					array.add(rule);
				}
				// 其它参数
				input = matcher.group(3);
				continue;
			}

			// 弹出错误
			throwable(message(FaultTip.INCORRECT_SYNTAX_X, input));
		}

		return array;
	}

}