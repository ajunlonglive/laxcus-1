/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import java.util.*;
import java.util.regex.*;

/**
 * 许可证标签
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public class LicenceToken {
	
	/** 正则表达式 **/
	private static final String REGEX = "^\\s*([0-9]{4})\\s*[\\.\\-\\_]\\s*([0-9]{1,2})\\s*[\\.\\-\\_]\\s*([0-9]{1,2})\\s*$";
	
	/** 一天的秒数 **/
	private static final long DAY_OF_MILLISECOND = 24 * 3600 * 1000;
	
	/** 有效开始时间 **/
	private long beginTime;
	
	/** 有效结束时间 **/
	private long endTime;
	
	/** 支持跨网段通信，默认是否 **/
	private boolean skipcast;

	/**
	 * 构造默认的许可证标签
	 */
	public LicenceToken() {
		super();
		beginTime = endTime = -1;
		setSkipcast(false);
	}

	/**
	 * 设置支持跨网段通信，如NAT网络
	 * @param b 真或者假
	 */
	public void setSkipcast(boolean b) {
		skipcast = b;
	}

	/**
	 * 判断支持跨网段通信，比如NAT网络，内网/公网
	 * @return 返回真或者假
	 */
	public boolean isSkipcast() {
		return skipcast;
	}
	
	/**
	 * 判断没有超时限制
	 * @return 返回真或者假
	 */
	public boolean isInfinite() {
		return (beginTime < 0 && endTime < 0) ;
	}
	
	/**
	 * 判断已经超时
	 * @return 返回真或者假
	 */
	public boolean isTimeout() {
		if (isInfinite()) {
			return false;
		}

		return System.currentTimeMillis() >= endTime;
	}
	
	/**
	 * 判断还有XXX天超期
	 * @param day 天数
	 * @return 返回
	 */
	public boolean isTimeout(long day) {
		// 无期限
		if (isInfinite()) {
			return false;
		}

		// 间隔时间
		long invterval = day * LicenceToken.DAY_OF_MILLISECOND;
		return System.currentTimeMillis() + invterval >= endTime;
	}
	
	/**
	 * 设置开始时间
	 * @param t
	 */
	public void setBeginTime(long t) {
		beginTime = t;
	}
	
	/**
	 * 返回开始时间
	 * @return
	 */
	public long getBeginTime() {
		return beginTime ;
	}

	/**
	 * 设置结束时间
	 * @param t
	 */
	public void setEndTime(long t) {
		endTime = t;
	}
	
	/**
	 * 返回结束时间
	 * @return
	 */
	public long getEndTime() {
		return endTime ;
	}
	
	/**
	 * 解析参数
	 * @param input
	 * @return
	 */
	private long splitTime(String input) {
		Pattern pattern = Pattern.compile(LicenceToken.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return -1;
		}

		int year = java.lang.Integer.parseInt(matcher.group(1));
		int month = java.lang.Integer.parseInt(matcher.group(2));
		int day = java.lang.Integer.parseInt(matcher.group(3));

		Calendar dar = Calendar.getInstance();
		dar.set(year, month - 1, day, 0, 0, 0);
		dar.set(Calendar.MILLISECOND, 0);
		return dar.getTime().getTime();
	}
	
	/**
	 * 解析开始时间
	 * @param input 输入参数
	 * @return 解析成功返回真，否则假
	 */
	public boolean splitBeginTime(String input) {
		long value = splitTime(input);
		boolean success = (value > 0);
		if (success) {
			beginTime = value;
		}
		return success;
	}
	
	/**
	 * 解析结束时间
	 * @param input 输入参数
	 * @return 解析成功返回真，否则假
	 */
	public boolean splitEndTime(String input) {
		long value = splitTime(input);
		boolean success = (value > 0);
		if (success) {
			endTime = value;
		}
		return success;
	}
}
