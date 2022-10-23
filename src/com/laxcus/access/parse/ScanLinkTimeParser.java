/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 定时扫描用户关联的间隔时间解析器
 * 
 * @author scott.liang
 * @version 1.0 6/3/2018
 * @since laxcus 1.0
 */
public class ScanLinkTimeParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX1 = "^\\s*(?i)(?:SET\\s+SCAN\\s+LINK\\s+TIME)\\s+([\\w\\W]+?)\\s+(?i)(REFRESH)\\s*$";

	private final static String REGEX2 = "^\\s*(?i)(?:SET\\s+SCAN\\s+LINK\\s+TIME)\\s+([\\w\\W]+?)\\s*$";
	
	/**
	 * 构造定时扫描用户关联的间隔时间解析器
	 */
	public ScanLinkTimeParser() {
		super();
	}

	/**
	 * 判断匹配定时扫描用户关联的间隔时间语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET SCAN LINK TIME", input);
		}
		Pattern pattern = Pattern.compile(ScanLinkTimeParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		if (!match) {
			pattern = Pattern.compile(ScanLinkTimeParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
		}
		return match;
	}

	/**
	 * 解析定时扫描用户关联的间隔时间语句
	 * @param input 输入语句
	 * @return 返回ScanLinkTime命令
	 */
	public ScanLinkTime split(String input) {
		ScanLinkTime cmd = new ScanLinkTime();
		// 保存命令原语
		cmd.setPrimitive(input);
		
		// 默认刷新
		boolean refresh = true;

		// 1. 第一种条件
		Pattern pattern = Pattern.compile(ScanLinkTimeParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean match = matcher.matches();
		// 2. 不匹配，第二种条件
		if (!match) {
			pattern = Pattern.compile(ScanLinkTimeParser.REGEX2);
			matcher = pattern.matcher(input);
			match = matcher.matches();
			refresh = false;
		}
		
		// 不匹配是错误
		if (!match) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		// 解析时间
		long interval = ConfigParser.splitTime(matcher.group(1), -1);
		if (interval < 1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		cmd.setInterval(interval);
		
		// 立即执行
		cmd.setImmediate(refresh);

		return cmd;
	}

}