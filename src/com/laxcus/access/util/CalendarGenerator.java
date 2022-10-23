/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.util;

import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.util.*;
import com.laxcus.util.datetime.*;

/**
 * 日期格式生成器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/12/2009
 * @since laxcus 1.0
 */
public final class CalendarGenerator {

	/**
	 * 日期格式: (年/月/日, 年-月-日, 年.月.日, 年月日). 如果年份采用两位数字，如"08"，默认是"2008"
	 * 时间格式: (时:分:秒   毫秒) 毫秒可省略，秒与毫秒之间由空格分隔
	 * 时间戳(日期时间)格式. 是日期和时间的组合，中间由空格分开.
	 */

	/** 日期格式 **/
	private final static String REGEX_DATE1 = "^\\s*([0-9]{4}|[0-9]{2})\\.([0-9]{1,2})\\.([0-9]{1,2})\\s*$";
	private final static String REGEX_DATE2 = "^\\s*([0-9]{4}|[0-9]{2})\\-([0-9]{1,2})\\-([0-9]{1,2})\\s*$";
	private final static String REGEX_DATE3 = "^\\s*([0-9]{4}|[0-9]{2})\\/([0-9]{1,2})\\/([0-9]{1,2})\\s*$";
	private final static String REGEX_DATE4 = "^\\s*([0-9]{4})([0-9]{2})([0-9]{2})\\s*$";

	/** 时间格式 **/
	private final static String REGEX_TIME1 = "^\\s*([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})(\\s+[0-9]{1,3}|\\s*)$";

	/** 时间格式，不包括秒和毫秒  **/
	private final static String REGEX_TIME2 = "^\\s*([0-9]{1,2}):([0-9]{1,2})\\s*$";
	
	/**
	 * 解析时间格式，返回一个int数值
	 * @param input 时间字符串
	 * @return 整型时间值
	 * @throws ColumnException
	 */
	public static int splitTime2(String input) {
		Pattern pattern = Pattern.compile(CalendarGenerator.REGEX_TIME2);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回-1
		if (!matcher.matches()) {
			return -1;
		}

		int hour = java.lang.Integer.parseInt(matcher.group(1));
		int minute = java.lang.Integer.parseInt(matcher.group(2));
		int second = 0;
		int ms = 0;

		// 检测时间格式
		if (!TimeChecker.check(hour, minute, second, ms)) {
			throw new IllegalValueException("time format error! %d:%d:%d %d", hour, minute, second, ms);
		}
		
		return SimpleTime.format(hour, minute, second, ms);
	}

	/**
	 * 解析时间格式，返回一个int数值
	 * @param input 时间字符串
	 * @return 整型时间值
	 * @throws ColumnException
	 */
	public static int splitTime1(final String input) {
		Pattern pattern = Pattern.compile(CalendarGenerator.REGEX_TIME1);
		Matcher matcher = pattern.matcher(input);

		// 不匹配，返回-1
		if (!matcher.matches()) {
			return -1;
		}

		int hour = java.lang.Integer.parseInt(matcher.group(1));
		int minute = java.lang.Integer.parseInt(matcher.group(2));
		int second = java.lang.Integer.parseInt(matcher.group(3));
		int ms = 0; // 毫秒
		String s = matcher.group(4).trim();
		if (s.length() > 0) ms = java.lang.Integer.parseInt(s);

		// 检测时间格式
		if (!TimeChecker.check(hour, minute, second, ms)) {
			throw new IllegalValueException("time format error! %d:%d:%d %d", hour, minute, second, ms);
		}

		return SimpleTime.format(hour, minute, second, ms);
	}

	/**
	 * 解析时间格式，返回一个int数值
	 * @param input 时间字符串
	 * @return 整型时间值
	 * @throws ColumnException
	 */
	public static int splitTime(final String input) {
		int time = splitTime1(input);
		if (time < 0) {
			time = splitTime2(input);
		}
		// 时间无效，弹出异常!
		if (time < 0) {
			throw new IllegalValueException("illegal time style: {%s}", input);
		}
		return time;
	}

	/**
	 * 解析日期格式，返回ini数值
	 * @param input 日期字符串
	 * @return 整型日期值
	 * @throws ColumnException
	 */
	public static int splitDate(final String input) {
		String[] regexs = { CalendarGenerator.REGEX_DATE1,
				CalendarGenerator.REGEX_DATE2, CalendarGenerator.REGEX_DATE3, 
				CalendarGenerator.REGEX_DATE4 };

		int year = 0, month = 0, day = 0;
		for (int i = 0; i < regexs.length; i++) {
			Pattern pattern = Pattern.compile(regexs[i]);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String s = matcher.group(1);
				if (s.length() == 2) {
					s = "20" + s;
				}
				year = java.lang.Integer.parseInt(s);
				month = java.lang.Integer.parseInt(matcher.group(2));
				day = java.lang.Integer.parseInt(matcher.group(3));
				
				// 判断日期参数在范围内
				if (!DateChecker.check(year, month, day)) {
					throw new IllegalValueException("date format error! %d-%d-%d", year, month, day);
				}
				
				return SimpleDate.format(year, month, day);
			}
		}

		throw new IllegalValueException("illegal date style: {%s}", input);
	}

	/**
	 * 解析时间戳格式，返回long数值
	 * @param input 时间戳字符串
	 * @return 长整型时间戳
	 * @throws ColumnException
	 */
	public static long splitTimestamp(String input) {
		// 找到空格
		if (input == null || input.trim().isEmpty()) {
			throw new NullPointerException("timestamp is null pointer!");
		}
		// 过滤两侧空格
		input = input.trim();
		
		int date = 0;
		int time = 0;
		// 找到日期/时间之间的空格
		int index = input.indexOf(0x20);

		// 如果只定义日期，不定义时间时...
		if (index == -1) {
			date = CalendarGenerator.splitDate(input);
		} else {
			String prefix = input.substring(0, index);
			String suffix = input.substring(index + 1);
			date = CalendarGenerator.splitDate(prefix);
			time = CalendarGenerator.splitTime(suffix);
		}
		
		// 生成时间戳
		java.util.Date dt = SimpleDate.format(date);
		java.util.Date tt = SimpleTime.format(time);

		Calendar instan = Calendar.getInstance();
		instan.setTime(dt);
		Calendar mode = Calendar.getInstance();
		mode.setTime(tt);

		instan.set(Calendar.HOUR_OF_DAY, mode.get(Calendar.HOUR_OF_DAY));
		instan.set(Calendar.MINUTE, mode.get(Calendar.MINUTE));
		instan.set(Calendar.SECOND, mode.get(Calendar.SECOND));
		instan.set(Calendar.MILLISECOND, mode.get(Calendar.MILLISECOND));

		return SimpleTimestamp.format(instan.getTime());
	}

	/**
	 * 生成日期列
	 * @param attribute 日期列属性
	 * @param input 日期字符串
	 * @return 日期列
	 */
	public static com.laxcus.access.column.Date createDate(
			DateAttribute attribute, String input) {
		int num = CalendarGenerator.splitDate(input);
		return new com.laxcus.access.column.Date(attribute.getColumnId(), num);
	}

	/**
	 * 生成日期列
	 * @param attribute 日期列属性
	 * @param value 日期数值
	 * @return 日期列
	 */
	public static com.laxcus.access.column.Date createDate(
			DateAttribute attribute, int value) {
		return new com.laxcus.access.column.Date(attribute.getColumnId(), value);
	}
	
	/**
	 * 生成日期列
	 * @param attribute 日期列属性
	 * @param date 日期数值
	 * @return 日期列
	 */
	public static com.laxcus.access.column.Date createDate(
			DateAttribute attribute, java.util.Date date) {
		return new com.laxcus.access.column.Date(attribute.getColumnId(), date);
	}
	
	/**
	 * 生成时间列
	 * @param attribute 时间列属性
	 * @param input 时间字符串
	 * @return 时间列
	 */
	public static com.laxcus.access.column.Time createTime(
			TimeAttribute attribute, String input) {
		int num = CalendarGenerator.splitTime(input);
		return new com.laxcus.access.column.Time(attribute.getColumnId(), num);
	}

	/**
	 * 生成时间列
	 * @param attribute 时间列属性
	 * @param value 时间值
	 * @return 时间列
	 */
	public static com.laxcus.access.column.Time createTime(
			TimeAttribute attribute, int value) {
		return new com.laxcus.access.column.Time(attribute.getColumnId(), value);
	}
	
	/**
	 * 生成时间列
	 * @param attribute 时间列属性
	 * @param date 时间值
	 * @return 时间列
	 */
	public static com.laxcus.access.column.Time createTime(
			TimeAttribute attribute, java.util.Date date) {
		return new com.laxcus.access.column.Time(attribute.getColumnId(), date);
	}
	
	/**
	 * 生成时间戳列
	 * @param attribute 时间戳列属性
	 * @param input 时间戳字符串
	 * @return 时间戳列
	 */
	public static com.laxcus.access.column.Timestamp createTimestamp(
			TimestampAttribute attribute, String input) {
		long num = CalendarGenerator.splitTimestamp(input);
		return new com.laxcus.access.column.Timestamp(attribute.getColumnId(), num);
	}
	
	/**
	 * 生成时间戳列
	 * @param attribute 时间戳列属性
	 * @param value 时间戳值
	 * @return 时间戳列
	 */
	public static com.laxcus.access.column.Timestamp createTimestamp(
			TimestampAttribute attribute, long value) {
		return new com.laxcus.access.column.Timestamp(attribute.getColumnId(), value);
	}
	
	/**
	 * 生成时间戳列
	 * @param attribute 时间戳列属性
	 * @param value 系统时间
	 * @return 时间戳列
	 */
	public static com.laxcus.access.column.Timestamp createTimestamp(
			TimestampAttribute attribute, java.util.Date value) {
		return new com.laxcus.access.column.Timestamp(attribute.getColumnId(), value);
	}
	
//	public static void main(String[] args) {
//		String st = "1993/3/4  ";
//		long dt = CalendarGenerator.splitTimestamp(st);
//		System.out.printf("%s\n", SimpleTimestamp.format(dt));
//	}
}