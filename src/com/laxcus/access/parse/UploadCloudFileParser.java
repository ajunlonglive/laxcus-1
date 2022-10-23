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
 * 上传文件解析器。<br><br>
 * 
 * 语法格式：UPLOAD FILE  -O -R|-L 本地文件名 TO 存储资源定义器 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/30/2021
 * @since laxcus 1.0
 */
public class UploadCloudFileParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:UPLOAD\\s+CLOUD\\s+FILE)\\s+(?i)(?:FROM)\\s+([\\w\\W]+)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";

	private static final String OV = "^\\s*(?i)(?:-O|-OVERRIDE)\\s+([\\w\\W]+?)(\\s*|\\s+[\\w\\W]+)$";
	
	private static final String FILE = "^\\s*(?i)(?:-F|-FILE)\\s+([\\w\\W]+?)(\\s*|\\s+[\\w\\W]+)$";
	
	/**
	 * 构造默认的上传文件解析器
	 */
	public UploadCloudFileParser() {
		super();
	}

	/**
	 * 判断匹配上传文件语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("UPLOAD CLOUD FILE", input);
		}
		Pattern pattern = Pattern.compile(UploadCloudFileParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 读文件
	 * @param file
	 * @return
	 */
	private byte[] readFile(File file) {
		int len = (int) file.length();
		try {
			byte[] b = new byte[len];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		} catch (IOException e) {

		}
		return null;
	}

	/**
	 * 解析前缀
	 * @param cmd
	 * @param input
	 */
	private void splitFrom(UploadCloudFile cmd, String input) {
		while (input.trim().length() > 0) {
			// 覆盖
			Pattern pattern = Pattern.compile(UploadCloudFileParser.OV);
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
			pattern = Pattern.compile(UploadCloudFileParser.FILE);
			matcher = pattern.matcher(input);
			if (matcher.matches()) {
				String filename = matcher.group(1);
				input = matcher.group(2);
				File file = new File(filename);
				// 判断文件有效
				boolean success = (file.exists() && file.isFile());
				if (!success) {
					throwableNo(FaultTip.NOTFOUND_X, filename);
				}
				// 读磁盘文件
				byte[] b = readFile(file);
				if (b == null) {
					throwableNo(FaultTip.FAILED_X, filename);
				}
				cmd.setContent(b);
				// cmd.setFile(file);
				continue;
			}
			
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回UploadFile命令
	 */
	public UploadCloudFile split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}

		Pattern pattern = Pattern.compile(UploadCloudFileParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		UploadCloudFile cmd = new UploadCloudFile();
		cmd.setPrimitive(input); // 保存原语
		
		String from = matcher.group(1);

		String to = matcher.group(2);
		if (!SRL.validate(to)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, to);
		}

		// 解析参数
		try {
			SRL srl = new SRL(to);
			cmd.setSRL(srl);
		} catch (UnknownHostException e) {
			throwable(e.getMessage());
		}
		
		// 解析前缀信息
		splitFrom(cmd, from);
		
		return cmd;
	}	

}
