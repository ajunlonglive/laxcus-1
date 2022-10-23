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
 * 时间格式化，包括:小时、分、秒、毫秒 <br>
 * <pre>
 *	millisecond: 10		0
 *	second:6			10
 *	minute:6			16
 *	hour: 5				22
 * </pre>
 * 
 *  @author scott.liang
 *  @version 1.2 12/3/2012
 *  @since laxcus 1.0
 */
public final class SimpleTime  {
	
	/** 最小/最大时间 **/
	public final static int MIN_VALUE = SimpleTime.format(0, 0, 0, 0);
	public final static int MAX_VALUE = SimpleTime.format(23, 59, 59, 999);

	/**
	 * 格式化时间为一个整数
	 * @param hour 小时
	 * @param minute 分钟
	 * @param second 秒
	 * @param millisecond 毫秒
	 * @return 返回整数值
	 */
	public static int format(int hour, int minute, int second, int millisecond) {
		// 检测时间格式
		if (!TimeChecker.check(hour, minute, second, millisecond)) {
			throw new IllegalValueException("time format error! %d:%d:%d %d", hour, minute, second, millisecond);
		}
		
		int value = ((hour & 0x1F) << 22);
		value |= ((minute & 0x3F) << 16);
		value |= ((second & 0x3F) << 10);
		value |= (millisecond & 0x3FF);
		return value;
	}

	/**
	 * 格式化时间为一个整数
	 * @param time Date实例
	 * @return 返回整数值
	 */
	public static int format(Date time) {
		Calendar dar = Calendar.getInstance();
		dar.setTime(time);
		int hour = dar.get(Calendar.HOUR_OF_DAY);
		int minute = dar.get(Calendar.MINUTE);
		int second = dar.get(Calendar.SECOND);
		int millisecond = dar.get(Calendar.MILLISECOND);
		return SimpleTime.format(hour, minute, second, millisecond);
	}

	/**
	 * 用当前时间格式化时间为一个整数
	 * @return 返回整数值
	 */
	public static int format() {
		Date date = new Date(System.currentTimeMillis());
		return SimpleTime.format(date);
	}

	/**
	 * 将时间数字格式为Date类型
	 * @param time 时间的数字格式
	 * @return 返回Date实例
	 */
	public static Date format(int time) {
		int hour = ((time >>> 22) & 0x1F);
		int minute = ((time >>> 16) & 0x3F);
		int second = ((time >>> 10) & 0X3F);
		int millisecond = time & 0x3FF;
		Calendar dar = Calendar.getInstance();

		dar.set(0, 0, 0);
		dar.set(Calendar.HOUR_OF_DAY, hour);
		dar.set(Calendar.MINUTE, minute);
		dar.set(Calendar.SECOND, second);
		dar.set(Calendar.MILLISECOND, millisecond);
		return dar.getTime();
	}

}