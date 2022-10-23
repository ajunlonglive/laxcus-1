/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.datetime;

/**
 * 时间检测器
 * 
 * @author scott.liang
 * @version 1.0 7/31/2020
 * @since laxcus 1.0
 */
public class TimeChecker {

	/**
	 * 检查时间参数在范围内
	 * @param hour
	 * @param minute
	 * @param second
	 * @param millisecond
	 * @return
	 */
	public static boolean check(int hour, int minute, int second, int millisecond) {
		// 判断小时
		if (!(0 <= hour && hour <= 23)) {
			return false;
		}
		// 判断分钟
		if (!(0 <= minute && minute <= 59)) {
			return false;
		}
		// 判断秒
		if (!(0 <= second && second <= 59)) {
			return false;
		}
		// 判断毫秒
		if (!(0 <= millisecond && millisecond <= 999)) {
			return false;
		}
		return true;
	}

}
