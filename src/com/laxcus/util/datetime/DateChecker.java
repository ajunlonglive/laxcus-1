/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.datetime;

/**
 * 日期检测器
 * 
 * @author scott.liang
 * @version 1.0 7/31/2020
 * @since laxcus 1.0
 */
public class DateChecker {

	/**
	 * 检查日期参数范围
	 * @param year
	 * @param month
	 * @param day
	 * @return 成功返回真，否则假
	 */
	public static boolean check(int year, int month, int day) {
		// 判断月份
		if (!(1 <= month && month <= 12)) {
			return false;
		}
		// 判断天
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			if (!(1 <= day && day <= 31)) {
				return false;
			}
			break;
		case 4:
		case 6:
		case 9:
		case 11:
			if (!(1 <= day && day <= 30)) {
				return false;
			}
			break;
		case 2:
			if (year % 4 == 0) { // 闰年
				if (!(1 <= day && day <= 29)) {
					return false;
				}
			} else { // 平年
				if (!(1 <= day && day <= 28)) {
					return false;
				}
			}
			break;
		}
		return true;
	}

}
