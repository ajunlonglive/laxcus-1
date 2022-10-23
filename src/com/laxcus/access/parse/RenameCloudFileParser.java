/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.net.*;
import java.util.regex.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 修改云端文件解析器。<br><br>
 * 
 * 语法格式：DROP DIRECTORY SRL (存储资源定义器) <br>
 * 
 * @author scott.liang
 * @version 1.0 10/27/2021
 * @since laxcus 1.0
 */
public class RenameCloudFileParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:RENAME\\s+CLOUD\\s+FILE)\\s+([\\w\\W]+)\\s+(?i)(?:AS)\\s+([\\w\\W&&[^:,;/\\\\]]+)\\s*$";

	/**
	 * 构造默认的修改云端文件解析器
	 */
	public RenameCloudFileParser() {
		super();
	}

	/**
	 * 判断匹配修改云端文件语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("RENAME CLOUD FILE", input);
		}
		Pattern pattern = Pattern.compile(RenameCloudFileParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回RenameCloudFile命令
	 */
	public RenameCloudFile split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(RenameCloudFileParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		RenameCloudFile cmd = new RenameCloudFile();
		cmd.setPrimitive(input); // 保存原语

		String text = matcher.group(1);
		String name = matcher.group(2);
		
		// 检测正确
		if (!SRL.validate(text)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, text);
		}

		// 解析参数
		try {
			SRL srl = new SRL(text);
			cmd.setSRL(srl);
		} catch (UnknownHostException e) {
			throwable(e.getMessage());
		}
		
		cmd.setName(name);
		return cmd;
	}	

}
