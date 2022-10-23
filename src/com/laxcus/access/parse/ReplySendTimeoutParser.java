/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;
import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 设置异步发送超时解析器 <BR><BR>
 * 
 * 格式：SET REPLY SEND TIMEOUT FIXP包失效时间 FIXP子包超时时间 FIXP子包发送间隔 TO [节点地址|ALL|LOCAL]
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class ReplySendTimeoutParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+REPLY\\s+SEND\\s+TIMEOUT)\\s+(?i)([0-9]+\\s*[^\\s]+?)\\s+(?i)([0-9]+\\s*[^\\s]+?)\\s+(?i)([0-9]+\\s*[^\\s]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的设置异步发送超时解析器
	 */
	public ReplySendTimeoutParser() {
		super();
	}
	
	/**
	 * 判断匹配“SET REPLY SEND TIMEOUT”语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SET REPLY SEND TIMEOUT", input);
		}
		Pattern pattern = Pattern.compile(ReplySendTimeoutParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SET REPLY SEND TIMEOUT”语句
	 * @param input 输入语句
	 * @return 返回ReplySendTimeout命令
	 */
	public ReplySendTimeout split(String input) {
		Pattern pattern = Pattern.compile(ReplySendTimeoutParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		ReplySendTimeout cmd = new ReplySendTimeout();
		
		// FIXP包失效时间
		String sub = matcher.group(1);
		long time = ConfigParser.splitTime(sub, 0);
		if (time < 0) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, sub);
		}
		cmd.setDisableTimeout(time);

		// FIXP子包失效时间
		sub = matcher.group(2);
		time = ConfigParser.splitTime(sub, 0);
		if (time < 0) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, sub);
		}
		cmd.setSubPacketTimeout(time);

		// FIXP子包失效时间
		sub = matcher.group(3);
		time = ConfigParser.splitTime(sub, 0);
		if (time < 0) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, sub);
		}
		cmd.setInterval(time);
		
		// 目标地址
		String suffix = matcher.group(4);
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}