/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.command.mix.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 密文超时解析器 <br>
 * 
 * 在“小时、分钟、秒”三个单位之间选择。
 * FRONT/WATCH站点的FIXP密文超时时间，交互密文将以此次的设置时间执行。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class CipherTimeoutParser extends SyntaxParser {

	/** FIXP远程客户端密文有效时间 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+CIPHER\\s+TIMEOUT)\\s+(.+?)\\s+(?i)(?:TO)\\s([\\w\\W]+)\\s*$";

	/**
	 * 构造密文超时解析器
	 */
	public CipherTimeoutParser() {
		super();
	}

	/**
	 * 判断匹配命令超时语句
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET CIPHER TIMEOUT", input);
		}
		Pattern pattern = Pattern.compile(CipherTimeoutParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析密文超时语句
	 * @param input 输入语句
	 * @return 返回CipherTimeout命令
	 */
	public CipherTimeout split(String input) {
		CipherTimeout cmd = new CipherTimeout();
		// 保存命令原语
		cmd.setPrimitive(input);

		// 解析判断
		Pattern pattern = Pattern.compile(CipherTimeoutParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String time = matcher.group(1);
		long interval = ConfigParser.splitTime(time, -1);
		if (interval == -1) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		cmd.setInterval(interval);

		// 目标地址
		String suffix = matcher.group(2);
		if (suffix.matches("^\\s*(?i)(LOCAL)\\s*$")) {
			cmd.setLocal(true);
		} else if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		return cmd;
	}

}