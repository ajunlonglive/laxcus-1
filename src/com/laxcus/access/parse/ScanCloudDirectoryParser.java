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
 * 扫描云端磁盘解析器。<br><br>
 * 
 * 语法格式：SCAN CLOUD DISK TO 存储资源定义器 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/30/2021
 * @since laxcus 1.0
 */
public class ScanCloudDirectoryParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SCAN\\s+CLOUD\\s+DIRECTORY)\\s+([\\w\\W]+?)\\s*$";
	
	private final static String FORMAT = "^\\s*(?i)(?:-FULL|-F)\\s+([Y|N|YES|NO]+?)\\s+([\\w\\W]+)\\s*$";

	/**
	 * 构造默认的扫描云端磁盘解析器
	 */
	public ScanCloudDirectoryParser() {
		super();
	}

	/**
	 * 判断匹配扫描云端磁盘语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("SCAN CLOUD DIRECTORY", input);
		}
		Pattern pattern = Pattern.compile(ScanCloudDirectoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回ScanCloudDisk命令
	 */
	public ScanCloudDirectory split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(ScanCloudDirectoryParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		ScanCloudDirectory cmd = new ScanCloudDirectory();
		cmd.setPrimitive(input); // 保存原语

		String text = matcher.group(1);
		
		// 前缀参数
		pattern = Pattern.compile(ScanCloudDirectoryParser.FORMAT);
		matcher = pattern.matcher(text);
		if (matcher.matches()) {
			String full = matcher.group(1);
			cmd.setFullPath(ConfigParser.splitBoolean(full, false));
			text = matcher.group(2);
		}
		
		// 解释
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
		return cmd;
	}	

}
