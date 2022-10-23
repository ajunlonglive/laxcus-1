/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.site.*;

/**
 * 获得数据块数据解析器。<br><br>
 * 
 * 语法格式：COPY ENTITY 数据库.表 数据块编号1, 数据块编号2 FROM 主节点地址 TO 备份节点地址
 * 
 * @author scott.liang
 * @version 1.0 11/10/2020
 * @since laxcus 1.0
 */
public class CopyEntityParser extends SyntaxParser {
	
//	private final static String REGEX_TITLE = "^\\s*(?i)(?:COPY\\s+ENTITY)\\s+([\\w\\W]+)\\s*$";
		
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:COPY\\s+ENTITY)\\s+([\\w\\W]+?)\\s+([\\w\\W]+?)\\s+(?i)(?:FROM)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+)\\s*$";
	
	/**
	 * 构造默认的获得数据块数据解析器
	 */
	public CopyEntityParser() {
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
			return isCommand("COPY ENTITY", input);
		}
		Pattern pattern = Pattern.compile(CopyEntityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析单元
	 * @param cmd
	 * @param stubs
	 * @param writeTo
	 */
	private List<Long> splitStubs( String stubs ) {
		ArrayList<Long> array = new ArrayList<Long>();
		String[] elements = splitCommaSymbol(stubs);
		for (String element : elements) {
			// 解析数据块编号
			long stub = ConfigParser.splitLong(element, 0);
			// 0值是无效的数据块编号
			if (stub == 0) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, element);
			}
			// 判断重复
			if (array.contains(stub)) {
				throwableNo(FaultTip.EXISTED_X, element);
			}
			// 保存编号
			array.add(stub);
		}
		return array;
	}

	/**
	 * 解析显示表语句，取出数据表名
	 * @param input 输入语句
	 * @param online 在线状态
	 * @return 返回CopyEntity命令
	 */
	public CopyEntity split(String input, boolean online) {
		// 检查是在线状态
		if (online) {
			checkOnline();
		}
		
		Pattern pattern = Pattern.compile(CopyEntityParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		// 以上不成立，弹出错误！
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		String prefix = matcher.group(1);
		String stub = matcher.group(2);
		String from = matcher.group(3); // 写入位置，磁盘目录或者文件
		String to = matcher.group(4);

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
		CopyEntity cmd = new CopyEntity(space);
		// 解析数据块编号
		List<Long> stubs = splitStubs(stub);
		cmd.addAll(stubs);
		
		if(!Node.validate(from)) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, from);
		}
		if(!Node.validate(to)) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, to);
		}
		
		try {
			cmd.setFrom(new Node(from));
			cmd.setTo(new Node(to));
		} catch (UnknownHostException e) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, from + " / " + to);
		}

		// 不允许同一个地址
		success = (Laxkit.compareTo(cmd.getFrom(), cmd.getTo()) == 0);
		if (success) {
			throwableNo(FaultTip.ILLEGAL_SITE_X, from + " / " + to);
		}
		
		// 保存原语
		cmd.setPrimitive(input);

		// 返回解析的命令
		return cmd;
	}
}