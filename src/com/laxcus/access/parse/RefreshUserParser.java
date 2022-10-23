/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.access.user.*;
import com.laxcus.util.tip.*;

/**
 * 刷新注册用户解析器<br><br>
 * 
 * 对应RefreshUser命令，命令由WATCH站点发出，通过HOME/TOP站点，作用到GATE站点。
 * GATE站点根据命令中的指定，跳过定时触发检查，立即更新指定的用户配置。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public final class RefreshUserParser extends MultiUserParser {

	/** 刷新注册用户语句 **/
	private final static String REGEX = "^\\s*(?i)(?:REFRESH\\s+USER)\\s+([\\w\\W]+?)\\s*$";
	
	/**
	 * 构造刷新注册用户解析器
	 */
	public RefreshUserParser() {
		super();
	}

	/**
	 * 判断匹配“REFRESH USER”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("REFRESH USER", input);
		}
		Pattern pattern = Pattern.compile(RefreshUserParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“REFRESH USER”语句
	 * @param input 输入语句
	 * @return 返回RefreshUser命令
	 */
	public RefreshUser split(String input) {
		Pattern pattern = Pattern.compile(RefreshUserParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String line = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String[] users = splitCommaSymbol(line);

		RefreshUser cmd = new RefreshUser();

		// 解析用户签名单元
		for (String username : users) {
			splitSigers(username, cmd);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}