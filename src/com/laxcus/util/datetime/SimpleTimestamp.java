/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.datetime;

import java.util.*;

import com.laxcus.util.*;

/**
 * 时间戳工具。<br>
 * 处理时间和long类型的转换，时间是以完整的日期格式，包括年、月、日、时、分、秒、毫秒 <br>
 *
 *<pre>
 * 	millisecond: 10
 * 	second:6		10
 * 	minute:6		16
 * 	hour: 5			22
 * 	day: 5			27
 * 	month: 4		32
 * 	year: 			36
 *</pre>
 * 
 *  @author scott.liang
 *  @version 1.0 12/5/2012
 *  @since laxcus 1.0
 */
public final class SimpleTimestamp {
	
	/** 最小时间 **/
	public final static long MIN_VALUE = SimpleTimestamp.format(1, 1, 1, 0, 0, 0, 0);

	/** 最大时间 **/
	public final static long MAX_VALUE = SimpleTimestamp.format(2999, 12, 31, 23, 59, 59, 999);
	
	/**
	 * 格式化日期时间,将参数合并为一个时间戳值
	 * @param year 年
	 * @param month 月
	 * @param day 日
	 * @param hour 小时
	 * @param minute 分钟
	 * @param second 秒
	 * @param millisecond 毫秒
	 * @return 返回长整数值
	 */
	public static long format(int year, int month, int day, int hour,
			int minute, int second, int millisecond) {
		// 判断日期溢出
		if (!DateChecker.check(year, month, day)) {
			throw new IllegalValueException("date format error! %d-%d-%d", year, month, day);
		}
		// 检测时间格式
		if (!TimeChecker.check(hour, minute, second, millisecond)) {
			throw new IllegalValueException("time format error! %d:%d:%d %d", hour, minute, second, millisecond);
		}
		
		// 毫秒
		long value = millisecond & 0x3FF;
		// 秒
		long v = (second & 0x3F);
		value |= (v << 10);
		// 分钟
		v = minute & 0x3F;
		value |= (v << 16);
		// 小时
		v = hour & 0x1F;
		value |= (v << 22);
		// 天
		v = day & 0x1F;
		value |= (v << 27);
		// 月
		v = month & 0xF;
		value |= (v << 32);
		// 年
		v = year & 0xFFFF;
		value |= (v << 36);
		// 返回结果
		return value;
	}

	/**
	 * 格式化时间戳为长整型值
	 * @param date Date实例
	 * @return 返回长整数值
	 */
	public static long format(Date date) {
		Calendar dar = Calendar.getInstance();
		dar.setTime(date);
		int year = dar.get(Calendar.YEAR);
		int month = dar.get(Calendar.MONTH) + 1;
		int day = dar.get(Calendar.DAY_OF_MONTH);
		int hour = dar.get(Calendar.HOUR_OF_DAY);
		int minute = dar.get(Calendar.MINUTE);
		int second = dar.get(Calendar.SECOND);
		int millisecond = dar.get(Calendar.MILLISECOND);
		return SimpleTimestamp.format(year, month, day, hour, minute, second, millisecond);
	}
	
	/**
	 * 把当前时间戳格式化为长整型值
	 * @return 返回长整数值
	 */
	public static long format() {
		Date date = new Date(System.currentTimeMillis());
		return SimpleTimestamp.format(date);
	}

	/**
	 * 输出一个标签格式的当前时间
	 * @return 长整数
	 */
	public static long currentTimeMillis(){
		Date date = new Date(System.currentTimeMillis());
		return SimpleTimestamp.format(date);
	}

	/**
	 * 把长整型时间戳转为系统时间
	 * @param time 时间戳
	 * @return - Date实例
	 */
	public static Date format(long time) {
		int year = (int)((time >>> 36) & 0xFFFF);
		int month = (int)((time >>> 32) & 0xF);
		int day = (int)((time >>> 27) & 0x1F);
		int hour = (int)((time >>> 22) & 0x1F);
		int minute = (int) ((time >>> 16) & 0x3F);
		int second = (int) ((time >>> 10) & 0x3F);
		int millisecond = (int) (time & 0x3FF);

		Calendar dar = Calendar.getInstance();
		dar.set(year, month - 1, day, hour, minute, second);
		dar.set(Calendar.MILLISECOND, millisecond);
		return dar.getTime();
	}

	

}