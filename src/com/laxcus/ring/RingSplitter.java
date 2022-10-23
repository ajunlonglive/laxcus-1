/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ring;

import java.net.*;
import java.util.regex.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 检测站点连通性解析器。<br><br>
 * 
 * 语法格式：RING -SECURE  -COUNT 1 -TIMEOUT 12S TO 目标FIXP服务器地址 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/1/2019
 * @since laxcus 1.0
 */
public class RingSplitter  {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:RING)(\\s+|\\s+.+\\s+)(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";
	
	/** -SECURE 安全加密通信 **/
	private final static String SECURE = "^\\s*(?i)(?:-SECURE|-S)\\s+(?i)(Y|N|YES|NO)(\\s*|\\s+.+)$";

	/** -COUNT 参数 **/
	private final static String COUNT = "^\\s*(?i)(?:-COUNT|-C)\\s+([1-9][0-9]*)(\\s*|\\s+.+)$";

	/** -TIMEOUT参数，socket超时 **/
	private final static String TIMEOUT = "^\\s*(?i)(?:-TIMEOUT|-T)\\s+([1-9][0-9]*[\\w\\W]+?)(\\s*|\\s+.+)$";

	/** -DELAY 延时间隔时间，允许参数是0 **/
	private final static String DELAY = "^\\s*(?i)(?:-DELAY|-D)\\s+([0-9][0-9]*[\\w\\W]+?)(\\s*|\\s+.+)$";

	/**
	 * 构造默认的检测站点连通性解析器
	 */
	public RingSplitter() {
		super();
	}
	
	/**
	 * 弹出异常
	 * @param input
	 */
	private void throwable(String input) {
		String s = String.format("Incorrect syntax: %s", input);
		throw new IllegalValueException(s);
	}

	/**
	 * 判断检测站点连通性语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(RingSplitter.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析参数
	 * @param cmd 命令
	 * @param input 输入语句
	 */
	private void splitParameters(Ring cmd, String input) {
		while (input.trim().length() > 0) {
			// -COUNT参数
			Pattern pattern = Pattern.compile(RingSplitter.COUNT);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int count = Integer.parseInt(matcher.group(1));
				// 发送次数统计
				cmd.setCount(count);
				input = matcher.group(2);
				continue;
			}

			// -TIMEOUT参数
			pattern = Pattern.compile(RingSplitter.TIMEOUT);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				long ms = ConfigParser.splitTime(text, -1);
				if (ms < 0) {
					throwable(input);
				}
				// socket超时
				cmd.setSocketTimeout((int) ms);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// -DELAY参数
			pattern = Pattern.compile(RingSplitter.DELAY);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				long ms = ConfigParser.splitTime(text, -1);
				if (ms < 0) {
					throwable(input);
				}
				// 延时间隔
				cmd.setDelay((int) ms);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// -SECURE参数
			pattern = Pattern.compile(RingSplitter.SECURE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				// 安全加密通信
				boolean secure = ConfigParser.splitBoolean(text, true);
				cmd.setSecure(secure);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// 错误
			throwable(input);
		}
	}

	/**
	 * 解析检测站点连通性命令
	 * @param input 输入语句
	 * @return 返回Ring命令
	 */
	public Ring split(String input) {
		Pattern pattern = Pattern.compile(RingSplitter.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 判断匹配
		if (!matcher.matches()) {
			throwable(input);
		}

		String prefix = matcher.group(1);
		String suffix = matcher.group(2);

		// 解析参数
		Ring cmd = new Ring();
		splitParameters(cmd, prefix);

		cmd.setPrimitive(input); 	// 保存原语
		cmd.setSocket(suffix); 		// socket明文

		// 解析地址
		try {
			SocketHost remote = new SocketHost(suffix);
			cmd.setRemote(remote);
		} catch (UnknownHostException e) {
			throwable(suffix);
		}

		return cmd;
	}

}