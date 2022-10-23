/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.net.*;
import java.util.regex.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.util.tip.*;

/**
 * 检测站点连通性解析器。<br><br>
 * 
 * 语法格式：RING -COUNT 1 -TIMEOUT 12S TO 目标FIXP服务器地址 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/1/2019
 * @since laxcus 1.0
 */
public class RingParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:RING)\\s+([\\w\\W]+)\\s*$";

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:RING)(\\s+|\\s+.+\\s+)(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";
	
	/** -SECURE 安全加密通信 **/
	private final static String SECURE = "^\\s*(?i)(?:-SECURE|-S)\\s+(?i)(Y|N|YES|NO|是|否)(\\s*|\\s+.+)$";

	/** -COUNT 参数 **/
	private final static String COUNT = "^\\s*(?i)(?:-COUNT|-C)\\s+([1-9][0-9]*)(\\s*|\\s+.+)$";

	/** -TIMEOUT参数，socket超时 **/
	private final static String TIMEOUT = "^\\s*(?i)(?:-TIMEOUT|-T)\\s+([1-9][0-9]*[\\w\\W]+?)(\\s*|\\s+.+)$";

	/** -DELAY 延时间隔时间，允许参数是0 **/
	private final static String DELAY = "^\\s*(?i)(?:-DELAY|-D)\\s+([0-9][0-9]*[\\w\\W]+?)(\\s*|\\s+.+)$";

	/**
	 * 构造默认的检测站点连通性解析器
	 */
	public RingParser() {
		super();
	}

	/**
	 * 判断检测站点连通性语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RING", input);
		}
		Pattern pattern = Pattern.compile(RingParser.REGEX);
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
			Pattern pattern = Pattern.compile(RingParser.COUNT);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				int count = Integer.parseInt(matcher.group(1));
				// 发送次数统计
				cmd.setCount(count);
				input = matcher.group(2);
				continue;
			}

			// -TIMEOUT参数
			pattern = Pattern.compile(RingParser.TIMEOUT);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				long ms = ConfigParser.splitTime(text, -1);
				if (ms < 0) {
					throwableNo(FaultTip.NOTRESOLVE_X, input);
				}
				// socket超时
				cmd.setSocketTimeout((int) ms);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// -DELAY参数
			pattern = Pattern.compile(RingParser.DELAY);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String text = matcher.group(1);
				long ms = ConfigParser.splitTime(text, -1);
				if (ms < 0) {
					throwableNo(FaultTip.NOTRESOLVE_X, input);
				}
				// 延时间隔
				cmd.setDelay((int) ms);
				// 后面的参数
				input = matcher.group(2);
				continue;
			}
			
			// -SECURE参数
			pattern = Pattern.compile(RingParser.SECURE);
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
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}
	}

	/**
	 * 解析检测站点连通性命令
	 * @param input 输入语句
	 * @param online 在线模式
	 * @return 返回Ring命令
	 */
	public Ring split(String input, boolean online) {
		// 检测在线模式
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(RingParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 判断匹配
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
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
			throwableNo(FaultTip.NOTRESOLVE_X, suffix);
		}

		return cmd;
	}

}
