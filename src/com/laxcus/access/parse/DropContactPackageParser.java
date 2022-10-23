/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.cloud.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 删除云端快速计算软件包解析器
 * 
 * @author scott.liang
 * @version 1.0 6/20/2020
 * @since laxcus 1.0
 */
public class DropContactPackageParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:DROP\\s+CONTACT\\s+PACKAGE)\\s+([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_\\-]]{1,16})(?i)(\\s*|\\s+FROM\\s+LOCAL\\s*)$";

	private final static String LOCAL_REGEX = "^\\s*(?i)(FROM\\s+LOCAL)\\s*$";
	
	/**
	 * 构造默认的删除云端快速计算软件包解析器
	 */
	public DropContactPackageParser() {
		super();
	}

	/**
	 * 检查传入的参数是否匹配语句
	 * @param simple 简单判断
	 * @param input  输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("DROP CONTACT PACKAGE", input);
		}
		Pattern pattern = Pattern.compile(DropContactPackageParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析删除命令
	 * 
	 * @param input 输入语句
	 * @return 返回DropContactPackage实例
	 */
	public DropContactPackage split(String input) {
		Pattern pattern = Pattern.compile(DropContactPackageParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		DropContactPackage cmd = new DropContactPackage();
		cmd.setPrimitive(input);

		// 软件名称
		String software = matcher.group(1);
		
		// 设置软件名称
		cmd.setWare(new Naming(software));
		
		// 删除云端在本地
		String local = matcher.group(2);
		cmd.setLocal(local.matches(LOCAL_REGEX));

		// 返回解析命令
		return cmd;
	}

}