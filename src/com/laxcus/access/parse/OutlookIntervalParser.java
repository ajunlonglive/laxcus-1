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
 * 被WATCH监视节点的定时刷新解析器 <br>
 * 
 * 在“小时、分钟、秒”三个单位之间选择。
 * WATCH节点监视所有在WATCH节点注册的节点，定时向他们请求节点状态。
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public class OutlookIntervalParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+OUTLOOK\\s+INTERVAL)\\s+([\\w\\W]+)\\s*$";
	
	/**
	 * 构造被WATCH监视节点的定时刷新解析器
	 */
	public OutlookIntervalParser() {
		super();
	}

	/**
	 * 判断匹配被WATCH监视节点的定时刷新语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET OUTLOOK INTERVAL", input);
		}
		Pattern pattern = Pattern.compile(OutlookIntervalParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析被WATCH监视节点的定时刷新语句
	 * @param input 输入语句
	 * @return 返回OutlookInterval命令
	 */
	public OutlookInterval split(String input) {
		OutlookInterval cmd = new OutlookInterval();
		// 保存命令原语
		cmd.setPrimitive(input);

		Pattern pattern = Pattern.compile(OutlookIntervalParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		String suffix = matcher.group(1);

		// 解析时间，如果错误返回-1
		long interval = ConfigParser.splitTime(suffix, -1);
		if (interval < 0) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, suffix);
		}
		// 设置时间
		cmd.setInterval(interval);

		return cmd;
	}

}