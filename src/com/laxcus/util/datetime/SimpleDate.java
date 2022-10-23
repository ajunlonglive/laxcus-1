/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.datetime;

import java.util.Calendar;
import java.util.Date;

import com.laxcus.util.*;

/**
 * 日期格式化。包括年、月、日三项
 * 
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public final class SimpleDate  {
	
	/** 最小/最大日期时间 **/
	public final static int MIN_VALUE = SimpleDate.format(1, 1, 1);
	public final static int MAX_VALUE = SimpleDate.format(2999, 12, 31);

	/**
	 * 格式化日期为一个整数
	 * @param year 年
	 * @param month 月
	 * @param day 日
	 * @return 返回整数值
	 * 
	 * @throws IllegalValueException 如果参数溢出
	 */
	public static int format(int year, int month, int day) {
		// 判断日期溢出
		if (!DateChecker.check(year, month, day)) {
			throw new IllegalValueException("date format error! %d-%d-%d", year, month, day);
		}
		
		int value = year & 0xFFFF;
		value <<= 9;
		value |= ((month & 0xF) << 5);
		value |= day & 0x1F;
		return value;
	}

	/**
	 * 格式化日期为一个整数
	 * @param date Date实例
	 * @return 返回整数值
	 */
	public static int format(Date date) {
		Calendar dar = Calendar.getInstance();
		dar.setTime(date);
		int year = dar.get(Calendar.YEAR);
		int month = dar.get(Calendar.MONTH) + 1;
		int day = dar.get(Calendar.DAY_OF_MONTH);
		return SimpleDate.format(year, month, day);
	}

	/**
	 * 用当前时间格式化日期为一个整数
	 * @return 返回整数值
	 */
	public static int format() {
		Date date = new Date(System.currentTimeMillis());
		return SimpleDate.format(date);
	}

	/**
	 * 将日期数字格式为Date类型
	 * @param date 日期的数字格式
	 * @return 返回Date实例
	 */
	public static Date format(int date) {
		int year = ((date >>> 9) & 0xFFFF);
		int month = ((date >>> 5) & 0xF);
		int day = date & 0x1F;
		Calendar dar = Calendar.getInstance();
		dar.set(year, month - 1, day, 0, 0, 0);
		dar.set(Calendar.MILLISECOND, 0);
		return dar.getTime();
	}

}