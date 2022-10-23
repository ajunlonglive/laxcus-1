/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import com.laxcus.command.cross.*;
import com.laxcus.law.cross.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;

/**
 * 资源共享解析器
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
class ActiveShareCrossParser extends SyntaxParser {

	/**
	 * 构造默认的资源共享解析器
	 */
	protected ActiveShareCrossParser() {
		super();
	}

	/**
	 * 解析操作符
	 * @param cmd 共享命令
	 * @param input 输入语句
	 */
	protected void splitOperators(ShareCross cmd, String input) {
		// 解析操作符
		int operator = CrossOperator.translate(input);
		// 判断是无效字符
		if (CrossOperator.isNone(operator)) {
			throwableNo(FaultTip.INCORRECT_SYNTAX_X, input);
		}
		// 设置操作符
		cmd.setOperator(operator);
	}

	/**
	 * 解析用户签名
	 * @param cmd 共享命令
	 * @param conferrers 被授权人明文字符串
	 */
	protected void splitSigers(ShareCross cmd, String conferrers) {
		// 以逗号切割字符串
		String[] items = splitCommaSymbol(conferrers);
		// 分析数据
		for (String item : items) {
			Siger conferrer = splitSiger(item); // 被授权人签名

			// 判断是账号持有人自己，弹出错误
			if (isPrivate(conferrer)) {
				throwableNo(FaultTip.INCORRECT_SYNTAX_X, item);
			}

			// 保存到被授权人
			cmd.addConferrer(conferrer);
			// 保存被授权人明文（实际明文或者SHA256码皆可）
			cmd.addText(item);
		}
	}

}