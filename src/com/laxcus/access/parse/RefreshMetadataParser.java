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
 * 刷新元数据解析器<br><br>
 * 
 * 对应RefreshMetadata命令。
 * 命令由WATCH站点发出，通过TOP/HOME站点，作用到DATA/WORK/BUILD站点，DATA/BUILD/WORK站点向CALL站点提交自己的资源元数据。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public class RefreshMetadataParser extends MultiUserParser {

	/** 命令语句 **/
	private final static String REGEX = "^\\s*(?i)(?:REFRESH\\s+METADATA)\\s+([\\w\\W]+?)\\s*$";

	/**
	 * 构造刷新元数据解析器
	 */
	public RefreshMetadataParser() {
		super();
	}

	/**
	 * 判断匹配“REFRESH METADATA”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("REFRESH METADATA", input);
		}
		Pattern pattern = Pattern.compile(RefreshMetadataParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析语句“REFRESH METADATA”语句
	 * @param input 输入语句
	 * @return 返回RefreshMetadata命令
	 */
	public RefreshMetadata split(String input) {
		Pattern pattern = Pattern.compile(RefreshMetadataParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String line = matcher.group(1);
		// 以逗号为依据，分割用户名称单元
		String[] items = splitCommaSymbol(line);

		RefreshMetadata cmd = new RefreshMetadata();

		// 解析用户签名单元
		for (String item : items) {
			splitSigers(item, cmd);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}