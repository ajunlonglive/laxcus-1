/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.access.account.*;
import com.laxcus.util.tip.*;

/**
 * 更新私有网络空间数据 <BR>
 * 
 * 语句：REFRESH CYBER
 * 
 * @author scott.liang
 * @version 1.0 5/31/2019
 * @since laxcus 1.0
 */
public class RefreshCyberParser extends SyntaxParser {
	
	/** 正则表达式  **/
	private final static String REGEX = "^\\s*(?i)(REFRESH\\s+CYBER)\\s*$";

	/**
	 * 构造默认的最大磁盘空间解析器
	 */
	public RefreshCyberParser() {
		super();
	}

	/**
	 * 判断语句匹配“REFRESH CYBER”
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(RefreshCyberParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析更新私有网络空间
	 * @param input 更新私有网络空间
	 * @return 返回RefreshCyber命令
	 */
	public RefreshCyber split(String input) {
		Pattern pattern = Pattern.compile(RefreshCyberParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		RefreshCyber cmd = new RefreshCyber(true);
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}
