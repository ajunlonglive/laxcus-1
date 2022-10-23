/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.util.tip.*;

/**
 * 条件解析基础类。<br>
 * 
 * 按照正则表达式的"贪婪算法"，采取从右向左匹配(最大化匹配)。<br><br>
 * 
 * 匹配说明：<br>
 * 表达式限定符后添加问号(?)，限定符将成为"勉强模式"，勉强模式限定符，总是尽可能少匹配。
 * 如果之后的表达式匹配失败，勉强模式也可以尽可能少的再匹配一些，以使整个表达式匹配成功。
 * 
 * @author scott.liang
 * @version 1.2 7/28/2012
 * @since laxcus 1.0
 */
public class GradationParser extends SyntaxParser {

	/** 截取表示式前面的逻辑连接符号 */
	protected final static String SQL_PART_LOGICPREFIX = "^\\s*(?i)(AND|OR)\\s+([\\w\\W]+)\\s*$";

	/** LIKE表达式 */
	protected final static String SQL_LIKE = "^\\s*([%_]*)([\\w\\W]+?)([%_]*)\\s*$";

	/**
	 * 构造条件解析器
	 */
	protected GradationParser() {
		super();
	}
	
	/**
	 * 分析忽略字数 ，-1表示无限制
	 * 
	 * @param symbol
	 * @return
	 */
	protected short getLikeSize(String symbol) {
		if (symbol.isEmpty()) {
			return 0;
		} else if ("%".equalsIgnoreCase(symbol)) {
			return -1; // 0xffff,无限制
		}
		
		for (int i = 0; i < symbol.length(); i++) {
			char w = symbol.charAt(i);
			if (w != '_') {
				// throw new SyntaxException("invalid like symbol:%s", symbol);
				throwableNo(FaultTip.SQL_ILLEGAL_VALUE_X, symbol);
			}
		}
		return (short) symbol.length();
	}
	
	/**
	 * 取出字符串两侧的"%","_"字符
	 * @param attribute
	 * @param input
	 * @return
	 */
	protected LikeString splitLike(ColumnAttribute attribute, String input) {
		if (!attribute.isWord()) {
//			throw new SyntaxException("invalid column:%s", attribute.getNameText());
			throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, attribute.getNameText());
		}
		WordAttribute consts = (WordAttribute) attribute;
		if (!consts.isLike()) {
//			throw new SyntaxException("cannot like by '%s'", attribute.getNameText());
			throwablePrefixFormat("Like", FaultTip.SQL_CANNOTSUPPORT_X, attribute.getNameText());
		}
		
		Pattern pattern = Pattern.compile(GradationParser.SQL_LIKE);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
//			throw new SyntaxException("invalid sql like:%s", input);
			throwableNo(FaultTip.SQL_ILLEGAL_COLUMN_X, input);
		}
		short left = getLikeSize(matcher.group(1));
		String text = matcher.group(2);
		short right = getLikeSize(matcher.group(3));

		LikeString string = new LikeString(left, right, text);
		string.setSentient(consts.isSentient());
		return string;
	}
	
	/**
	 * 过滤外层的括号， 取出中间数据(外层括号必须是匹配的)
	 * 
	 * @param input 输入语句
	 * @return 处理结果
	 */
	protected String filteBrackets(String input) {
		final String regex = "^\\s*\\(([\\w\\W]+)\\)\\s*$";
		do {
			// 1. 表达式是否匹配,不匹配退出
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(input);
			if (!matcher.matches()) {
				break;
			}

			// 2. 检查最旁边两侧的括号是否对称
			int index = 0, seek = 0;
			boolean begin = false, ignore = false;
			while (seek < input.length()) {
				char w = input.charAt(seek++);
				if (w == '\'') {
					ignore = !ignore;
				} else if (ignore) {
					continue;
				} else if (w == '(') { // 在第一个括号前,必须是空字符
					if(!begin) {
						String s = input.substring(0, seek - 1);
						begin = s.trim().isEmpty();
					}
					index++;
				} else if (w == ')') {
					index--;
				}
			}

			// 首字符必须是左括号,括号对必须匹配
			if (begin && index == 0) {
				input = matcher.group(1);
				continue;
			}
		} while (false);

		return input;
	}

	/**
	 * 将一个WHERE查询语句，找到最外层的"AND|OR|("(AND|OR加上左括号)分割点进行分割
	 * 
	 * 按照括号对进行分组,判断条件是:
	 * 1. 左括号'('之前必须有 "AND|OR"
	 * 2. 右括号')'之后必须有 "AND|OR"
	 * 
	 * @param input
	 * @return
	 */
	protected String[] splitGroup(String input) {
		//1. 过滤两侧无意义的括号
		input = this.filteBrackets(input);
		//2. 对WHERE语句进行分组
		int index = 0, begin = 0, seek = 0;
		boolean ignore = false;
		List<String> array = new ArrayList<String>();
		while (seek < input.length()) {
			char w = input.charAt(seek++);
			
			if (w == '\'') { // 过滤单引号之间的数据
				if (seek - 2 >= 0) {
					w = input.charAt(seek - 2);
					if (w == '\\') continue;
				}
				ignore = !ignore;
			} else if (ignore) { // 在单引号之间的数据
				continue;
			} else if (w == '(') {
				if (index == 0) {
					final String regex = "^([\\w\\W]+)(\\s+(?i)(?:AND|OR)\\s+\\()$";
					String prefix = input.substring(begin, seek); // 取前缀
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(prefix); 
					if (matcher.matches()) {
						prefix = matcher.group(1);
						if (prefix.trim().length() > 0) array.add(prefix);
						begin += prefix.length();
					}
				}				
				index++;
			} else if (w == ')') {
				index--;
				if (index != 0) continue; // 没有归0,不处理				
				
				final String regex = "^(\\)\\s+(?i)(?:AND|OR)\\s+)([\\w\\W]+)$";
				String suffix = input.substring(seek - 1);
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(suffix); 
				if (matcher.matches()) {
					String prefix = input.substring(begin, seek);
					if (prefix.trim().length() > 0) array.add(prefix);
					begin = seek;
				}
			}
		}
		
		if (ignore || index != 0) {
			throwableNo(FaultTip.NOTRESOLVE_X, input);
			// throw new SyntaxException("cannot resolve '%s'", input);
		}
		
		if(begin < input.length()) {
			array.add(input.substring(begin));
		}

		String[] s = new String[array.size()];		
		return array.toArray(s);
	}
}