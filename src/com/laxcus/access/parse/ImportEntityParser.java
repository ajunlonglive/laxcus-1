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

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.tip.*;

/**
 * 数据导入集群解析器。<br><br>
 * 
 * 语法格式：IMPORT ENTITY 数据库.表 FROM 磁盘文件名1, 磁盘文件2, ...  TYPE [CSV|TXT] CHARSET [UTF-8|GBK|UTF-16] SECTION 每次从文件中读取的行数
 * 
 * @author scott.liang
 * @version 1.0 5/11/2019
 * @since laxcus 1.0
 */
public class ImportEntityParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:IMPORT\\s+ENTITY)\\s+([\\w\\W]+)\\s*$";
	
	/** 正则表达式1，带参数 **/
	private final static String REGEX1 = "^\\s*(?i)(?:IMPORT\\s+ENTITY)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+(.+?)(?i)((\\s+TYPE|CHARSET|SECTION\\s+)([\\w\\W]+))$";

	/** 字符集 **/
	private final static String CHARSET = "^\\s*(?i)(?:CHARSET)\\s+(?i)([\\w\\W]+?)(\\s+.+?|\\s*)$";
	
	/** 类型定义 **/
	private final static String TYPE = "^\\s*(?i)(?:TYPE)\\s+(?i)([\\w\\W]+?)(\\s+.+?|\\s*)$";
	
	/** 单次读取的文件数据记录长度  **/
	private final static String SECTION = "^\\s*(?i)(?:SECTION)\\s+([1-9][0-9]*)(\\s+.+|\\s*)$";

	/**
	 * 构造默认的数据导入集群解析器
	 */
	public ImportEntityParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("IMPORT ENTITY", input);
		}
		// 1. 第一种情况
		Pattern pattern = Pattern.compile(ImportEntityParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析剩余参数
	 * @param input 输入语句
	 * @param cmd 命令实例 
	 */
	private void splitParams(String input, ImportEntity cmd) {
		String suffix = input;
		
		// 逐个解析，直到完成 ！
		while (input.trim().length() > 0) {
			// 文件类型
			Pattern pattern = Pattern.compile(ImportEntityParser.TYPE);
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
			pattern = Pattern.compile(ImportEntityParser.CHARSET);
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

			// 读取的行数
			pattern = Pattern.compile(ImportEntityParser.SECTION);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				// 行数
				int rows  = Integer.parseInt( matcher.group(1));
				cmd.setRows(rows);
				// 其它数据
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
	 * @return 返回ImportEntity命令
	 */
	public ImportEntity split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		// 第一种条件
		Pattern pattern = Pattern.compile(ImportEntityParser.REGEX1);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		// 以上不成立，弹出错误！
		if (!success) {
			throwable(FaultTip.INCORRECT_SYNTAX);
		}

		String tableText = matcher.group(1);
		String diskText = matcher.group(2);
		String params = matcher.group(3);

		// 判断数据表有效
		if (!Space.validate(tableText)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, tableText);
		}
		Space space = new Space(tableText);
		// 如果是在线模式，判断
		if (online) {
			// 检查表有效
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, tableText);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, tableText);
			}
			// 检查导入数据的权限
			if(!canTable(space, ControlTag.IMPORT_ENTITY)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, tableText);
			}
		}
		
		// 生成命令
		ImportEntity cmd = new ImportEntity(space);
		
		// 解析磁盘文件
		String[] elements = splitCommaSymbol(diskText);
		for(String filename : elements) {
			File file = new File(filename);
			// 磁盘文件必须存在，否则是错误！
			success = (file.exists() && file.isFile());
			// 不存在，弹出异常
			if (!success) {
				throwableNo(FaultTip.NOTFOUND_X, filename);
			}
			// 保存磁盘文件
			cmd.add(file);
		}
		
		// 解析剩余字符，文件字符集编码和文件类型
		splitParams(params, cmd);
		
		// 保存原语
		cmd.setPrimitive(input);

		return cmd;
	}

}