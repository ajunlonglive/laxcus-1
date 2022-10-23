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
 * 检索用户在集群的分布区域解析器<br><br>
 * 
 * 对应SeekUserArea命令，命令由WATCH站点发出。
 * 如果在TOP节点，检索范围包括BANK/HOME集群。如果在BANK/HOME集群，检索其下节点。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2019
 * @since laxcus 1.0
 */
public final class SeekUserAreaParser extends MultiUserParser {

	/** 命令表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SEEK\\s+USER\\s+AREA)\\s+([\\w\\W]+?)\\s*$";
	
	/**
	 * 构造检索用户在集群的分布区域解析器
	 */
	public SeekUserAreaParser() {
		super();
	}

	/**
	 * 判断匹配“SEEK USER AREA”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SEEK USER AREA", input);
		}
		Pattern pattern = Pattern.compile(SeekUserAreaParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SEEK USER AREA”语句
	 * @param input 输入语句
	 * @return 返回SeekUserArea命令
	 */
	public SeekUserArea split(String input) {
		Pattern pattern = Pattern.compile(SeekUserAreaParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String line = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String[] users = splitCommaSymbol(line);

		SeekUserArea cmd = new SeekUserArea();

		// 解析用户签名单元
		for (String username : users) {
			splitSigers(username, cmd);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}