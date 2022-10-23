/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.File;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.tip.*;

/**
 * 获得数据块数据解析器。<br><br>
 * 
 * 语法格式：EXPORT ENTITY 数据库.表  数据块编号1, 数据块编号2  TO DIRECTORY 磁盘目录 | TO FILES 磁盘文件 TYPE [CSV|TXT] CHARSET [UTF-8|GBK|UTF-16|UTF-32]
 * 
 * @author scott.liang
 * @version 1.0 2/11/2018
 * @since laxcus 1.0
 */
public class ExportEntityParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:EXPORT\\s+ENTITY)\\s+([\\w\\W]+)\\s*$";
		
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:EXPORT\\s+ENTITY)\\s+([\\w\\W]+?)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)(\\s+(?i)(CHARSET|TYPE)\\s+([\\w\\W]+)|\\s*)$";
	
	/** 字符集 **/
	private final static String CHARSET = "^\\s*(?i)(?:CHARSET)\\s+(?i)([\\w\\W]+?)(\\s+.+?|\\s*)$";
	
	/** 类型定义 **/
	private final static String TYPE = "^\\s*(?i)(?:TYPE)\\s+(?i)([\\w\\W]+?)(\\s+.+?|\\s*)$";
	
	/**
	 * 构造默认的获得数据块数据解析器
	 */
	public ExportEntityParser() {
		super();
	}

	/**
	 * 判断匹配显示数据表语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 匹配返回“真”，否则“假”。
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("EXPORT ENTITY", input);
		}
		Pattern pattern = Pattern.compile(ExportEntityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析剩余参数
	 * @param input
	 * @param cmd
	 */
	private void splitParams(String input, ExportEntity cmd) {
		// 逐个解析，直到完成 ！
		while (input.trim().length() > 0) {
			// 文件类型
			Pattern pattern = Pattern.compile(ExportEntityParser.TYPE);
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

				input = matcher.group(2);
				continue;
			}

			// 字符集
			pattern = Pattern.compile(ExportEntityParser.CHARSET);
			matcher = pattern.matcher(input);
			success = matcher.matches();
			if (success) {
				String charset = matcher.group(1);
				int type = CharsetType.translate(charset);
				// 判断符合定义
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
	}
	
	/**
	 * 根据目录生成文件
	 * @param stub
	 * @param dir
	 * @param type
	 * @return
	 */
	private File createFile(long stub, File dir, String type) {
		String who = String.format("%x.%s", stub, type);
		File file = new File(dir, who);
		if (!file.exists()) {
			return file;
		}
		for (int index = 1; true; index++) {
			who = String.format("%x_%d.%s", stub, index, type);
			file = new File(dir, who);
			if (!file.exists()) {
				return file;
			}
		}
	}

	/**
	 * 判断名称是目录
	 */
	private boolean isDirectory(String name) {
		String[] names = splitCommaSymbol(name);
		// 只有1个
		boolean success = (names.length == 1);
		// 判断是目录
		if (success) {
			File dir = new File(name);
			success = (dir.exists() && dir.isDirectory());
		}
		return success;
	}
	
	/**
	 * 解析单元
	 * @param cmd
	 * @param stubs
	 * @param writeTo
	 */
	private void splitItem(ExportEntity cmd, String stubs, String writeTo) {
		ArrayList<Long> a = new ArrayList<Long>();
		String[] elements = splitCommaSymbol(stubs);
		for (String element : elements) {
			// 解析数据块编号
			long id = ConfigParser.splitLong(element, 0);
			// 0值是无效的数据块编号
			if (id == 0) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, element);
			}
			// 保存编号
			a.add(id);
		}
		
		// 判断是目录
		boolean success = isDirectory(writeTo);
		// 如果是目录，以编码为名称，生成全路径文件名
		if (success) {
			// 转成对应的文件名后缀
			String type = EntityStyle.translate(cmd.getType()).toLowerCase();
			File dir = new File(writeTo);
			for (long id : a) {
				File file = createFile(id, dir, type);
				// 保存单元
				ExportEntityItem item = new ExportEntityItem(file, id);
				cmd.add(item);
			}
		}
		// 不成立，判断是文件名。条件：
		// 1. 文件数目必须与编号数目一致
		// 2. 每个文件名不能存在，但是父目录必须存在
		else {
			String[] filenames = splitCommaSymbol(writeTo);
			if (filenames.length != a.size()) {
				throwableNo(FaultTip.NOTMATCH_X, stubs + " <-> " + writeTo);
			}
			
			for (int i = 0; i < filenames.length; i++) {
				String filename = filenames[i];
				// 保证是一个新文件，磁盘不存在这个文件。
				File file = new File(filename);
				// 如果文件存在，弹出异常提示
				success = (file.exists() && file.isFile());
				if (success) {
					throwableNo(FaultTip.EXISTED_X, filename);
				}
				
				// 取它的父目录，如果不存在，是错误！
				File parent = file.getParentFile();
				success = (parent.exists() && parent.isDirectory());
				if (!success) {
					throwableNo(FaultTip.NOTFOUND_X, parent.toString());
				}

				// 保存单元
				ExportEntityItem item = new ExportEntityItem(file, a.get(i));
				cmd.add(item);
			}
		}
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回ExportEntity命令
	 */
	public ExportEntity split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(ExportEntityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		// 以上不成立，弹出错误！
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String prefix = matcher.group(1);
		String stub = matcher.group(2);
		String writeTo = matcher.group(3); // 写入位置，磁盘目录或者文件
		String params = matcher.group(4);

		// 判断表名有效
		if (!Space.validate(prefix)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, prefix);
		}

		// 生成数据表名
		Space space = new Space(prefix);
		// 在线检查表
		if (online) {
			// 表存在
			if (!hasTable(space)) {
				throwableNo(FaultTip.NOTFOUND_X, prefix);
			}
			// 如果是授权表，直接拒绝！
			if (isPassiveTable(space)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, prefix);
			}
			// 系统允许这个操作？
			if (!canTable(space, ControlTag.EXPORT_ENTITY)) {
				throwableNo(FaultTip.PERMISSION_MISSING_X, prefix);
			}
		}
		
		// 生成命令
		ExportEntity cmd = new ExportEntity(space);
		
		// 补充参数
		if (params != null) {
			splitParams(params, cmd);
		}
		
		// 解析数据块单元
		splitItem(cmd, stub, writeTo);

		// 保存原语
		cmd.setPrimitive(input);

		// 返回解析的命令
		return cmd;
	}
}