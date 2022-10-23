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
 * 检索用户在线注册元数据解析器<br><br>
 * 
 * 对应SeekRegisterMetadata命令，命令由WATCH站点发出，通过HOME/TOP站点，作用到CALL/WORK/BUILD/DATA站点。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public final class SeekRegisterMetadataParser extends MultiUserParser {

	/** 命令表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SEEK\\s+REGISTER\\s+METADATA)\\s+([\\w\\W]+?)\\s*$";
	
	/**
	 * 构造检索用户在线注册元数据解析器
	 */
	public SeekRegisterMetadataParser() {
		super();
	}

	/**
	 * 判断匹配“SEEK REGISTER METADATA”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("SEEK REGISTER METADATA", input);
		}
		Pattern pattern = Pattern.compile(SeekRegisterMetadataParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“SEEK REGISTER METADATA”语句
	 * @param input 输入语句
	 * @return 返回SeekRegisterMetadata命令
	 */
	public SeekRegisterMetadata split(String input) {
		Pattern pattern = Pattern.compile(SeekRegisterMetadataParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String line = matcher.group(1);
		// 以逗号为依据，分割用户签名
		String[] users = splitCommaSymbol(line);

		SeekRegisterMetadata cmd = new SeekRegisterMetadata();

		// 解析用户签名单元
		for (String username : users) {
			splitSigers(username, cmd);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}