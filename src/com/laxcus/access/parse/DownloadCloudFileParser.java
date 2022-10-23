/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.net.*;
import java.util.regex.*;

import com.laxcus.command.cloud.store.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 下载文件解析器。<br><br>
 * 
 * 语法格式：DOWNLOAD FILE FROM 存储资源定义器 TO -O -R|-L 本地文件名  <br>
 * 
 * @author scott.liang
 * @version 1.0 10/30/2021
 * @since laxcus 1.0
 */
public class DownloadCloudFileParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:DOWNLOAD\\s+CLOUD\\s+FILE)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	private static final String OV = "^\\s*(?i)(?:-O|-OVERRIDE)\\s+([\\w\\W]+?)(\\s*|\\s+[\\w\\W]+)$";
	
	private static final String FILE = "^\\s*(?i)(?:-F|-FILE)\\s+([\\w\\W]+?)(\\s*|\\s+[\\w\\W]+)$";

	/**
	 * 构造默认的上传文件解析器
	 */
	public DownloadCloudFileParser() {
		super();
	}

	/**
	 * 判断匹配上传文件语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("DOWNLOAD CLOUD FILE", input);
		}
		Pattern pattern = Pattern.compile(DownloadCloudFileParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析前缀
	 * @param cmd
	 * @param input
	 */
	private boolean splitSuffix(DownloadCloudFile cmd, String input) {
		while (input.trim().length() > 0) {
			// 覆盖
			Pattern pattern = Pattern.compile(DownloadCloudFileParser.OV);
			Matcher matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String value = matcher.group(1);
				boolean b = ConfigParser.isBoolean(value);
				if (!b) {
					throwableNo(FaultTip.INCORRECT_SYNTAX_X, value);
				}
				input = matcher.group(2);
				boolean yes = ConfigParser.splitBoolean(value, false);
				cmd.setOverride(yes);
				continue;
			}

			// 本地文件
			pattern = Pattern.compile(DownloadCloudFileParser.FILE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String filename = matcher.group(1);
				input = matcher.group(2);

				// 判断文件有效
				File file = new File(filename);
				cmd.setFile(file);
				continue;
			}

			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		return true;
	}

	/**
	 * 解析命令
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回DownloadFile命令
	 */
	public DownloadCloudFile split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(DownloadCloudFileParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		DownloadCloudFile cmd = new DownloadCloudFile();
		cmd.setPrimitive(input); // 保存原语

		String from = matcher.group(1);
		if (!SRL.validate(from)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, from);
		}
		// 解析参数
		try {
			SRL srl = new SRL(from);
			cmd.setSRL(srl);
		} catch (UnknownHostException e) {
			throwable(e.getMessage());
		}

		// 解析后缀信息
		String suffix = matcher.group(2);
		// 不成功，返回假
		boolean success = splitSuffix(cmd, suffix);
		if (!success) {
			return null;
		}

		return cmd;
	}

}