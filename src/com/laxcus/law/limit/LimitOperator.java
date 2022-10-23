/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import com.laxcus.law.rule.*;
import com.laxcus.util.*;

/**
 * 限制操作符。<br>
 * 
 * 分为读操作和写操作两种，它们和事务规则操作符（RuleOperator）中的读写对应。匹配即是限制。
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class LimitOperator {

	/** 无定义 **/
	public final static byte NONE = 0;

	/** 读限制操作 */
	public final static byte READ = 1;

	/** 写限制操作 **/
	public final static byte WRITE = 2;

	/**
	 * 判断是“写限制”操作
	 * @param who 限制操作符
	 * @return 返回真或者假
	 */
	public static boolean isWrite(byte who) {
		return who == LimitOperator.WRITE;
	}

	/**
	 * 判断是“读限制”操作
	 * @param who 限制操作符
	 * @return 返回真或者假
	 */
	public static boolean isRead(byte who) {
		return who == LimitOperator.READ;
	}

	/**
	 * 判断是合法的限制操作
	 * @param who 限制操作符
	 * @return 返回真或者假
	 */
	public static boolean isOperator(byte who) {
		return isRead(who) || isWrite(who) ;
	}

	/**
	 * 判断限制操作和事务操作存在冲突。它们的前提是用户签名一致，这项由外部方法保证。
	 * @param limit 限制操作符
	 * @param rule 事务操作符
	 * @return 冲突返回真，否则返回假
	 */
	public static boolean conflict(byte limit, byte rule) {
		// 判断是合法的操作符
		if (!LimitOperator.isOperator(limit)) {
			throw new IllegalValueException("illegal operator: %d", limit);
		}
		if (!RuleOperator.isOperator(rule)) {
			throw new IllegalValueException("illegal operator: %d", rule);
		}

		/** 发生匹配即冲突 **/
		if (LimitOperator.isWrite(limit) && !RuleOperator.isShareRead(rule)) {
			return true;
		} else if(LimitOperator.isRead(limit) && RuleOperator.isShareRead(rule)) {
			return true;
		}
		// 不是冲突
		return false;
	}

	/**
	 * 将限制操作操作符翻译为字符串描述
	 * @param operator 操作符
	 * @return 字符串
	 */
	public static String translate(byte operator) {
		switch (operator) {
		case LimitOperator.WRITE:
			return "WRITE";
		case LimitOperator.READ:
			return "READ";
		}
		return "ILLEGAL LIMIT OPERATOR";
	}

	/**
	 * 将限制操作操作符字符串翻译为数字描述
	 * @param input 输入语句
	 * @return 限制操作操作符
	 */
	public static byte translate(String input) {
		if (input.matches("^\\s*(?i)(WRITE)\\s*$")) {
			return LimitOperator.WRITE;
		} else if (input.matches("^\\s*(?i)(READ)\\s*$")) {
			return LimitOperator.READ;
		}
		return -1;
	}
}
