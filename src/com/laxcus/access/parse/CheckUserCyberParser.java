/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.*;
import java.util.regex.*;

import com.laxcus.command.cyber.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 检测集群中用户虚拟空间解析器 <br>
 * 
 * 语法：CHECK USER CYBER  TO [ALL|site address, ....]
 * 只限WATCH节点管理员操作。
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class CheckUserCyberParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:CHECK\\s+USER\\s+CYBER\\s+TO)\\s+(.+?)\\s*$";

	/**
	 * 构造检测集群中用户虚拟空间解析器
	 */
	public CheckUserCyberParser() {
		super();
	}

	/**
	 * 判断匹配语句
	 * @param simple 简单判断
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(boolean simple, String input) {
		if(simple) {
			return isCommand("CHECK USER CYBER", input);
		}
		Pattern pattern = Pattern.compile(CheckUserCyberParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析检测集群中用户虚拟空间语句
	 * @param input 输入语句
	 * @return 返回CheckUserCyber命令
	 */
	public CheckUserCyber split(String input) {
		// 解析判断
		Pattern pattern = Pattern.compile(CheckUserCyberParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配是错误
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		
		String suffix = matcher.group(1);

		// 定义命令
		CheckUserCyber cmd = new CheckUserCyber();
		
		// 全部，指定节点
		if (suffix.matches("^\\s*(?i)(ALL)\\s*$")) {

		} else {
			byte[] families = new byte[] { SiteTag.ACCOUNT_SITE,
					SiteTag.GATE_SITE, SiteTag.CALL_SITE, SiteTag.DATA_SITE,
					SiteTag.WORK_SITE, SiteTag.BUILD_SITE };
			List<Node> nodes = splitSites(suffix, families);
			cmd.addAll(nodes);
		}

		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}

}