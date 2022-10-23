/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.io;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * 平面样式文本读取器，包括CSV和TXT格式
 * 
 * @author soctt.liang
 * @version 1.0 5/5/2019
 * @since laxcus 1.0
 */
public abstract class PlainRowReader extends StyleRowReader {

	/** 过滤两侧的引号，4种可能性，权重从1开始到4 **/
	
	private static final String regex1 = "^(?:[\"]{3})(.*?)(?:[\"]{3})$";

	private static final String regex2 = "^(?:[\"]{3})(.*?)(?:[\"]{1})$";
	
	private static final String regex3 = "^(?:[\"]{1})(.*?)(?:[\"]{3})$";
	
	private static final String regex4 = "^(?:[\"]{1})(.*?)(?:[\"]{1})$";

	/**
	 * 构造默认的平面样式文本读取器
	 */
	protected PlainRowReader() {
		super();
	}

	/**
	 * 构造平面样式文本读取器，指定文件
	 * @param file 文件
	 */
	protected PlainRowReader(File file) {
		super(file);
	}

	/**
	 * 构造平面样式文本读取器，指定文件和编码类型
	 * @param file 文件
	 * @param charset 编码格式
	 */
	protected PlainRowReader(File file, int charset) {
		super(file, charset);
	}

	/**
	 * 过滤引号，包括两侧和中间的引号
	 * @param input 输出字符
	 * @return 过滤后的字符
	 */
	private String filte(String input) {
		// 第一种情况
		Pattern pattern = Pattern.compile(PlainRowReader.regex1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (success) {
			input = '\"' + matcher.group(1) + '\"';
		}
		// 第二种情况
		if (!success) {
			pattern = Pattern.compile(PlainRowReader.regex2);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				input = '\"' + matcher.group(1);
			}
		}
		// 第三种情况
		if (!success) {
			pattern = Pattern.compile(PlainRowReader.regex3);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				input = matcher.group(1) + '\"';
			}
		}
		// 第四种情况
		if (!success) {
			pattern = Pattern.compile(PlainRowReader.regex4);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				input = matcher.group(1);
			}
		}
		
		// 替换字符串里的引号
		input = input.replaceAll("\"\"", "\"");

		return input;
	}

	/**
	 * 从磁盘中读取一行记录，以列的字符串数组样式返回
	 * @param separator 分隔符
	 * @return 返回字符串数组
	 * @throws IOException
	 */
	protected String[] readRow(char separator) throws IOException {
		ensureOpen();
		
		int count = 0;
		int seek = 0;

		StringBuilder bf = new StringBuilder();
		// 从磁盘中提取一行记录，以回车换行符为结束
		for (int index = 0; true; index++) {
			int c = reader.read();
			// 达到末尾，退出
			if (c == -1) {
				break;
			}

			// 保存字符
			char w = (char) c;
			bf.append(w);

			// 以下条件要判断
			if (w == '\"') {
				count++; // 统计双引号
			} else if (w == '\r') {
				seek = index; // 记录回车下标
			} else if (w == '\n') {
				// 达到以下条件退出
				// 1. 是回车换行符
				// 2. 引号没有或者是双数
				if (seek + 1 == index && (count == 0 || count % 2 == 0)) {
					break;
				}
			}
		}

		if (bf.length() == 0) {
			return null;
		}
		
		// 取一行
		String line = bf.toString();
		// 过滤尾部的回车换行符
		if (line.endsWith("\r\n")) {
			line = line.substring(0, line.length() - 2);
		}

		// 以逗号为分隔符，提取字段
		ArrayList<String> a = new ArrayList<String>();
		count = 0;
		seek = 0;
		int len = line.length();
		for (int i = 0; i < len; i++) {
			char w = line.charAt(i);
			// 统计引号
			if (w == '\"') {
				count++;
			} else if (w == separator) {
				// 找到逗号分隔符，判断引号是双数才是结尾。截取这一段字符串，做为一个字段
				if (count == 0 || count % 2 == 0) {
					String sub = line.substring(seek, i);
					// 移到下一字段开始位置，统计值复位0
					seek = i + 1;
					count = 0; 
					// 过滤掉可能存在的引号
					sub = filte(sub);
					a.add(sub);
				}
			}
		}
		
		if (seek < len) {
			String sub = line.substring(seek, line.length());
			sub = filte(sub);
			a.add(sub);
		} else if (seek == len) {
			a.add(""); // 空字段
		}
		
		// 输出字符串数组
		String[] s = new String[a.size()];
		return a.toArray(s);
	}

	

}
