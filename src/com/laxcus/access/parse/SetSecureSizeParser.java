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

import com.laxcus.command.secure.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 设置对称密钥长度解析器 <BR><BR>
 * 
 * 格式: SET SECURE SIZE -client xxx -server xxx TO [ 节点地址 ]
 * 
 * @author scott.liang
 * @version 1.0 2/27/2021
 * @since laxcus 1.0
 */
public class SetSecureSizeParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SET\\s+SECURE\\s+SIZE)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s([\\w\\W]+?)\\s*$";
	
	/** 客户机对称密钥数位 **/
	private final static String CLIENT = "^\\s*(?i)(?:-CLIENT|-C)\\s+(?i)([1-9][0-9]*?)(\\s*|\\s+.+)$";
	
	/** 服务器对称密钥数位 **/
	private final static String SERVER = "^\\s*(?i)(?:-SERVER|-S)\\s+(?i)([1-9][0-9]*?)(\\s*|\\s+.+)$";

	/**
	 * 构造默认的设置对称密钥长度解析器
	 */
	public SetSecureSizeParser() {
		super();
	}

	/**
	 * 判断匹配“SET SECURE SIZE”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SET SECURE SIZE", input);
		}
		Pattern pattern = Pattern.compile(SetSecureSizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解密密钥数位，必须是8的倍数
	 * @param input 输入参数
	 * @return 返回对称值
	 */
	private int splitBits(String input) {
		// 判断是数字
		if (!ConfigParser.isInteger(input)) {
			throwableNo(FaultTip.ILLEGAL_VALUE_X, input);
		}
		// 解析数位，必须大0，而且是8的数位
		int keysize = ConfigParser.splitInteger(input, -1);
		boolean success = (keysize > 0 && keysize % 8 == 0);
		if (!success) {
			throwableNo(FaultTip.ILLEGAL_VALUE_X, input);
		}
		return keysize;
	}

	/**
	 * 解析参数
	 * @param cmd 命令
	 * @param input 输入语句
	 */
	private void splitParameters(SetSecureSize cmd, String input) {
		while (input.trim().length() > 0) {
			// -CLIENT参数
			Pattern pattern = Pattern.compile(SetSecureSizeParser.CLIENT);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String value = matcher.group(1);
				input = matcher.group(2);

				// 解析参数
				cmd.setClientBits(this.splitBits(value));
				continue;
			}

			// -SERVER参数
			pattern = Pattern.compile(SetSecureSizeParser.SERVER);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String value = matcher.group(1);
				input = matcher.group(2);

				// 解析参数
				cmd.setServerBits(this.splitBits(value));
				continue;
			}
			
			// 错误
			throwableNo(FaultTip.NOTRESOLVE_X, input);
		}
	}

	/**
	 * 解析“SET SECURE SIZE”语句
	 * @param input 输入语句
	 * @return 返回SetSecureSize命令
	 */
	public SetSecureSize split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(SetSecureSizeParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		SetSecureSize cmd = new SetSecureSize();

		// 解析参数
		String prefix = matcher.group(1);
		String suffix = matcher.group(2);

		// 解析参数
		splitParameters(cmd, prefix);

		// 解析节点地址
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