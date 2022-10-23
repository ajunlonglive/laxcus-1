/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.File;
import java.util.regex.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.tip.*;

/**
 * 检测数据文件内容解析器。<br><br>
 * 
 * 语法格式：CHECK ENTITY CONTENT 数据库.表  FROM 磁盘文件  TYPE [CSV|TXT] CHARSET [UTF-8|GBK|UTF-16] 
 * 
 * @author scott.liang
 * @version 1.0 6/11/2019
 * @since laxcus 1.0
 */
public class CheckEntityContentParser extends SyntaxParser {

	/** 正则表达式  **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+ENTITY\\s+CONTENT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+(.+?)(\\s+(?i)(TYPE|CHARSET)\\s+(.+))\\s*$"; 
	
	//"^\\s*(?i)(?:CHECK\\s+ENTITY\\s+CONTENT)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+(.+?)\\s+(.+?)\\s*$";
	
	/** 字符集 **/
	private final static String CHARSET = "^\\s*(?i)(?:CHARSET)\\s+(?i)([\\w\\W]+?)(\\s+.+?|\\s*)$";
	
	/** 类型定义 **/
	private final static String TYPE = "^\\s*(?i)(?:TYPE)\\s+(?i)([\\w\\W]+?)(\\s+.+?|\\s*)$";
	
	/**
	 * 构造默认的检测数据文件内容解析器
	 */
	public CheckEntityContentParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("CHECK ENTITY CONTENT", input);
		}
		Pattern pattern = Pattern.compile(CheckEntityContentParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析剩余参数
	 * @param input 输入语句
	 * @param cmd 命令实例 
	 */
	private void splitSuffix(String input, CheckEntityContent cmd) {
		String suffix = input;
		
		// 逐个解析，直到完成 ！
		while (input.trim().length() > 0) {
			// 文件类型
			Pattern pattern = Pattern.compile(CheckEntityContentParser.TYPE);
			Matcher	matcher = pattern.matcher(input);
			boolean success = matcher.matches();
			if (success) {
				String who = matcher.group(1);
				// 转义描述字
				int type = EntityStyle.translate(who);
				if (!EntityStyle.isType(type)) {
					throwableNo(FaultTip.NOTRESOLVE_X, who);
				}
				cmd.setType(type);
				// 继续下一个
				input = matcher.group(2);
				continue;
			}

			// 字符集
			pattern = Pattern.compile(CheckEntityContentParser.CHARSET);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				String charset = matcher.group(1);
				// 判断字符类型
				int type = CharsetType.translate(charset);
				if (!CharsetType.isCharset(type)) {
					throwableNo(FaultTip.NOTRESOLVE_X, charset);
				}
				cmd.setCharset(type);

				input = matcher.group(2);
				continue;
			}

			// 弹出错误
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		
		// 如果不定义字符集，可忽略！留到运行时检查。
		
		// 判断解析有效
		if (!EntityStyle.isType(cmd.getType())) {
			throwableNo(FaultTip.PARAM_MISSING_X, suffix);
		}
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回CheckEntityContent命令
	 */
	public CheckEntityContent split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(CheckEntityContentParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		// 以上不成立，弹出错误！
		if (!success) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		// 数据表
		String text = matcher.group(1);
		// 判断表名有效
		if (!Space.validate(text)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, text);
		}
		Space space = new Space(text);
		// 如果是在线模式，判断
		if (online) {
			// 检查表有效
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, text);
			}
		}

		// 生成命令
		CheckEntityContent cmd = new CheckEntityContent(space);

		String diskText = matcher.group(2);
		// 解析磁盘文件
		String[] elements = splitCommaSymbol(diskText);
		for(String filename : elements) {
			File file = new File(filename);
			success = (file.exists() && file.isFile());
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, filename);
			}
			// 保存磁盘文件
			cmd.add(file);
		}

		//		// 磁盘文件必须已经存在，否则是错误！
		//		String filename = matcher.group(2);
		//		File file = new File(filename);
		//		success = (file.exists() && file.isFile());
		//		if (!success) {
		//			throwable(FaultTip.NOTFOUND_X, filename);
		//		}
		//		cmd.setFile(file);

		// 解析剩余字符，文件字符集编码和文件类型
		String params = matcher.group(3);
		splitSuffix(params, cmd);

		// 保存原语
		cmd.setPrimitive(input);

		return cmd;
	}
	
//	public void fuck() {
//		String s = "check entity content govdata.enterprise  FROM d:\\downloads\\enterprise(1).csv TYPE CSV CHARSET UTF-8";
//		CheckEntityContent cmd =	this.split(s, false);
//		System.out.printf(s);
//	}
//	
//	public static void main(String[] args) {
//		CheckEntityContentParser e = new CheckEntityContentParser();
//		e.fuck();
//	}

}