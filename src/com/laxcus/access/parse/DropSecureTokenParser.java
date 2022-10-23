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
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 删除密钥令牌解析器 <BR><BR>
 * 
 * 格式: DROP SECURE TOKEN [令牌名称] FROM [ 节点地址 ]
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class DropSecureTokenParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:DROP\\s+SECURE\\s+TOKEN)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s([\\w\\W]+?)\\s*$";

	/**
	 * 构造默认的删除密钥令牌解析器
	 */
	public DropSecureTokenParser() {
		super();
	}
	
	/**
	 * 判断匹配“DROP SECURE TOKEN”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("DROP SECURE TOKEN", input);
		}
		Pattern pattern = Pattern.compile(DropSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析命名
	 * @param prefix
	 * @return
	 */
	private List<Naming> splitNames(String prefix) {
		String[] strings = splitCommaSymbol(prefix);
		ArrayList<Naming> array = new ArrayList<Naming>();
		for (String str : strings) {
			array.add(new Naming(str));
		}
		return array;
	}

	/**
	 * 解析“DROP SECURE TOKEN”语句
	 * @param input 输入语句
	 * @return 返回ReloadSecure命令
	 */
	public DropSecureToken split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(DropSecureTokenParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		DropSecureToken cmd = new DropSecureToken();

		// 解析密钥令牌名称
		String prefix = matcher.group(1);
		cmd.addNames(splitNames(prefix));

		// 解析节点地址
		String suffix = matcher.group(2);
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}