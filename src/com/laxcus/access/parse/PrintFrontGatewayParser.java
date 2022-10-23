/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.util.regex.*;

import com.laxcus.command.mix.*;
import com.laxcus.util.tip.*;

/**
 * 打印FRONT网关解析器 <br>
 * 
 * @author scott.liang
 * @version 1.0 02/15/2018
 * @since laxcus 1.0
 */
public class PrintFrontGatewayParser extends SyntaxParser {

	/** 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:PRINT\\s+FRONT\\s+GATEWAY)\\s*$";

	/**
	 * 建立打印FRONT网关解析器
	 */
	public PrintFrontGatewayParser() {
		super();
	}

	/**
	 * 判断语句匹配
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(PrintFrontGatewayParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析语句
	 * @param input 输入语句
	 * @return 返回PrintFrontGateway命令
	 */
	public PrintFrontGateway split(String input) {
		Pattern pattern = Pattern.compile(PrintFrontGatewayParser.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}

		PrintFrontGateway cmd = new PrintFrontGateway();
		// 保存命令原语
		cmd.setPrimitive(input);

		return cmd;
	}
}