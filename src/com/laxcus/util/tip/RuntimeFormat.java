/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.tip;

import java.util.regex.*;

import com.laxcus.util.*;

/**
 * 格式化运行时间。<br>
 * 此操作要求按照即定的规则执行。<br><br>
 * 
 * 格式如：“本次操作耗时 {[DD]天/[HH]小时/[MM]分钟/[SS]秒/[MS]毫秒}”<br>
 * 其中"{...}"是格式符，最外层；"/"是格式符，分割参数；"[...]"是格式符，描述时间。“DD HH MM SS MS”是关键字。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/12/2016
 * @since laxcus 1.0
 */
public class RuntimeFormat {

	private final static String REGEX = "^\\s*(.*?)\\{(.+?)\\}(.*?)$";

	private final static String DAY = "^(.*?)(\\[DD\\])(.*?)$";
	private final static String HOUR = "^(.*?)(\\[HH\\])(.*?)$";
	private final static String MINUTE = "^(.*?)(\\[MM\\])(.*?)$";
	private final static String SECOND = "^(.*?)(\\[SS\\])(.*?)$";
	private final static String MS = "^(.*?)(\\[MS\\])(.*?)$";

	/**
	 * 构造格式化运行时间
	 */
	public RuntimeFormat() {
		super();
	}

	/**
	 * 判断匹配
	 * @param regex 正则表达式
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean isMatch(String regex, String input) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

//	/**
//	 * 组合参数
//	 * @param regex 正则表达式
//	 * @param input 输入语句
//	 * @param time 时间
//	 * @return 组合后的字符串
//	 */
//	private String combine(String regex, String input, long time) {
//		// 小于1忽略
//		if (time < 1) {
//			return "";
//		}
//		// 格式化和转换参数
//		Pattern pattern = Pattern.compile(regex);
//		Matcher matcher = pattern.matcher(input);
//		// 整个匹配一次
//		matcher.matches();
//		// 取出前后两组参数
//		String prefix = matcher.group(1);
//		String suffix = matcher.group(3);
//		return String.format("%s%d%s", prefix, time, suffix);
//	}
//
//	/**
//	 * 格式化参数
//	 * @param input 输入语句
//	 * @param time 时间
//	 * @return 格式化结果
//	 */
//	public String format(String input, long time) {
//		Pattern pattern = Pattern.compile(REGEX);
//		Matcher matcher = pattern.matcher(input);
//		if (!matcher.matches()) {
//			throw new IllegalValueException(input);
//		}
//
//		String prefix = matcher.group(1);
//		String middle = matcher.group(2);
//		String suffix = matcher.group(3);
//
//		// 天
//		long day = time / Laxkit.DAY;
//		// 小时
//		long hour = (time - day * Laxkit.DAY) / Laxkit.HOUR;
//		// 分钟
//		long minute = (time - day * Laxkit.DAY - hour * Laxkit.HOUR) / Laxkit.MINUTE;
//		// 秒
//		long second = (time - day * Laxkit.DAY - hour * Laxkit.HOUR - minute * Laxkit.MINUTE) / Laxkit.SECOND;
//		// 毫秒
//		long ms = time - day * Laxkit.DAY - hour * Laxkit.HOUR - minute * Laxkit.MINUTE - second * Laxkit.SECOND;
//
//		StringBuilder buf = new StringBuilder();
//
//		// 以"/"做为分割符
//		String[] items = middle.split("\\/");
//		// 重组参数
//		for (String item : items) {
//			if (isMatch(RuntimeFormat.DAY, item)) {
//				buf.append(combine(RuntimeFormat.DAY, item, day));
//			} else if (isMatch(RuntimeFormat.HOUR, item)) {
//				buf.append(combine(RuntimeFormat.HOUR, item, hour));
//			} else if (isMatch(RuntimeFormat.MINUTE, item)) {
//				buf.append(combine(RuntimeFormat.MINUTE, item, minute));
//			} else if (isMatch(RuntimeFormat.SECOND, item)) {
//				buf.append(combine(RuntimeFormat.SECOND, item, second));
//			} else if (isMatch(RuntimeFormat.MS, item)) {
//				buf.append(combine(RuntimeFormat.MS, item, ms));
//			} else {
//				throw new IllegalValueException(item);
//			}
//		}
//		
//		return prefix + buf.toString() + suffix;
//	}

	
	/**
	 * 组合参数
	 * @param regex 正则表达式
	 * @param input 输入语句
	 * @param time 时间
	 * @return 组合后的字符串
	 */
	private String combine(String regex, String input, long time) {
		// 格式化和转换参数
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 整个匹配一次
		matcher.matches();
		// 取出前后两组参数
		String prefix = matcher.group(1);
		String suffix = matcher.group(3);
		return String.format("%s%d%s", prefix, time, suffix);
	}

	/**
	 * 格式化参数
	 * @param input 输入语句
	 * @param time 时间
	 * @return 格式化结果
	 */
	public String format(String input, long time) {
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new IllegalValueException(input);
		}

		String prefix = matcher.group(1);
		String middle = matcher.group(2);
		String suffix = matcher.group(3);

		// 天
		long day = time / Laxkit.DAY;
		// 小时
		long hour = (time - day * Laxkit.DAY) / Laxkit.HOUR;
		// 分钟
		long minute = (time - day * Laxkit.DAY - hour * Laxkit.HOUR) / Laxkit.MINUTE;
		// 秒
		long second = (time - day * Laxkit.DAY - hour * Laxkit.HOUR - minute * Laxkit.MINUTE) / Laxkit.SECOND;
		// 毫秒
		long ms = time - day * Laxkit.DAY - hour * Laxkit.HOUR - minute * Laxkit.MINUTE - second * Laxkit.SECOND;

		StringBuilder buf = new StringBuilder();

		// 以"/"做为分割符
		String[] items = middle.split("\\/");
		
		// 重组参数
		for (String item : items) {
			String value = "";
			long seek = 0;
			if (isMatch(RuntimeFormat.DAY, item)) {
				value = combine(RuntimeFormat.DAY, item, seek = day);
			} else if (isMatch(RuntimeFormat.HOUR, item)) {
				value = combine(RuntimeFormat.HOUR, item, seek = hour);
			} else if (isMatch(RuntimeFormat.MINUTE, item)) {
				value = combine(RuntimeFormat.MINUTE, item, seek = minute);
			} else if (isMatch(RuntimeFormat.SECOND, item)) {
				value = combine(RuntimeFormat.SECOND, item, seek = second);
			} else if (isMatch(RuntimeFormat.MS, item)) {
				value = combine(RuntimeFormat.MS, item, seek = ms);
			} else {
				throw new IllegalValueException(item);
			}
			
			if (buf.length() > 0) {
				buf.append(value);
			} else if (seek > 0) {
				buf.append(value);
			}
		}

		return prefix + buf.toString() + suffix;
	}
	
	public void test() {
//		String input = "{[DD]days /[HH] hours /[MM] minutes /[SS] seconds /[MS] milli-seconds}";
//		String input = "当前耗时 {[DD] /[HH]:/[MM]:/[SS]./[MS]} 时间";
		String input = "{[DD] /[HH]:/[MM]:/[SS]./[MS]}";
		String text = this.format(input, 111153002L);
		System.out.println(text);
	}
	
	public static void main(String[] args){
		new RuntimeFormat().test();
	}

}