/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;
import java.util.regex.*;
import java.util.*;

import com.laxcus.command.licence.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 发布许可证解析器 <BR><BR>
 * 
 * 格式：PUBLISH LICENCE xxx TO [节点地址|ALL]
 * 
 * @author scott.liang
 * @version 1.0 7/21/2020
 * @since laxcus 1.0
 */
public class PublishLicenceParser extends SyntaxParser {
	
	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PUBLISH\\s+LICENCE)\\s+([\\w\\W]+?)\\s+(?i)(?:TO)\\s+([\\w\\W]+?)(?i)(\\s+-IE|\\s*)$";

	private final static String REGEX_IE = "^\\s*(?i)(-IE)\\s*$";
	
	/**
	 * 构造默认的发布许可证解析器
	 */
	public PublishLicenceParser() {
		super();
	}
	
	/**
	 * 判断匹配“PUBLISH LICENCE”语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if (simple) {
			return isCommand("PUBLISH LICENCE", input);
		}
		Pattern pattern = Pattern.compile(PublishLicenceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析“PUBLISH LICENCE”语句
	 * @param input 输入语句
	 * @return 返回PublishLicence命令
	 */
	public PublishLicence split(String input) {
		Pattern pattern = Pattern.compile(PublishLicenceParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		PublishLicence cmd = new PublishLicence();

		String filename = matcher.group(1);
		File file = new File(filename);
		// 判断文件存在
		success = (file.exists() && file.isFile());
		if (!success) {
			throwableNo(FaultTip.NOTFOUND_X, filename);
		}
		cmd.setFile(file);
		
		// 节点
		String suffix = matcher.group(2);
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			List<Node> nodes = splitSites(suffix);
			cmd.addAll(nodes);
		}
		
		// 判断是否需要立即执行
		String ie = matcher.group(3);
		if(ie.matches(REGEX_IE)) {
			cmd.setImmediate(true);
		}

		cmd.setPrimitive(input);

		return cmd;
	}

}
