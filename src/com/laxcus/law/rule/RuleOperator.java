/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.rule;

import com.laxcus.util.*;

/**
 * 事务操作符号 <br><br>
 * 
 * 事务操作符号分为三种：共享读、共享写、独享写，它们之间的关系是：<br>
 * 
 * 1.共享读（和共享写是并存关系，即多个共享读和共享写从FRONT站点发出后，AID站点受理它们，允许它们同时执行；和互斥写是排它关系，只允许一个存在）<br>
 * 2.共享写（和共享读是并存，和互斥写是排它关系）<br>
 * 3.独享写（与共享读和共享写是排它关系，即独享写被AID站点受理后，在它完成前，只允许这一个独享写事务存在）<br>
 * 
 * @author scott.liang
 * @version 1.0 4/1/2013
 * @since laxcus 1.0
 */
public final class RuleOperator {

	/** 共享读操作（允许多个共享读在AID站点和其它站点同时发生） **/
	public final static byte SHARE_READ = 1;

	/** 共享写操作（允许多个写操作在AID站点同时发生，在最终执行站点采用串行模式） **/
	public final static byte SHARE_WRITE = 2;

	/** 独享写操作（只允许一个写操作在AID站点发生） **/
	public final static byte EXCLUSIVE_WRITE = 3;

	/**
	 * 判断是合法的事务操作符
	 * @param who 事务操作符
	 * @return 返回真或者假
	 */
	public static boolean isOperator(byte who) {
		switch (who) {
		case RuleOperator.SHARE_READ:
		case RuleOperator.SHARE_WRITE:
		case RuleOperator.EXCLUSIVE_WRITE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是共享读
	 * @param who 事务操作符
	 * @return 返回真或者假
	 */
	public static boolean isShareRead(byte who) {
		return who == RuleOperator.SHARE_READ;
	}

	/**
	 * 判断是共享写
	 * @param who 事务操作符
	 * @return 返回真或者假
	 */
	public static boolean isShareWrite(byte who) {
		return who == RuleOperator.SHARE_WRITE;
	}

	/**
	 * 判断是独享写
	 * @param who 事务操作符
	 * @return 返回真或者假
	 */
	public boolean isExclusiveWrite(byte who) {
		return who == RuleOperator.EXCLUSIVE_WRITE;
	}

	/**
	 * 判断两个操作符冲突。
	 * @param b1 操作符1
	 * @param b2 操作符2
	 * @return 冲突返回真，否则返回假
	 */
	public static boolean conflict(byte b1, byte b2) {
		// 判断是合法的操作符
		if (!RuleOperator.isOperator(b1)) {
			throw new IllegalValueException("illegal operator: %d", b1);
		}
		if (!RuleOperator.isOperator(b2)) {
			throw new IllegalValueException("illegal operator: %d", b2);
		}

		// 判断冲突
		switch (b1) {
		case RuleOperator.SHARE_READ:
		case RuleOperator.SHARE_WRITE:
			return (b2 == RuleOperator.EXCLUSIVE_WRITE); // 只与独享写冲突
		case RuleOperator.EXCLUSIVE_WRITE:
			return true; // 与任何一个都冲突（因为是独享）
		}
		throw new IllegalValueException("illegal operator: %d,%d", b1, b2);
	}

	/**
	 * 将事务操作符数字翻译为字符串描述
	 * @param operator 事务操作符
	 * @return 事务操作符字符串
	 */
	public static String translate(byte operator) {
		switch (operator) {
		case RuleOperator.SHARE_READ:
			return "SHARE READ";
		case RuleOperator.SHARE_WRITE:
			return "SHARE WRITE";
		case RuleOperator.EXCLUSIVE_WRITE:
			return "EXCLUSIVE WRITE";
		}
		return "ILLEGAL RULE OPERATOR";
	}
	
	/**
	 * 将事务操作符字符串翻译为数字描述
	 * @param input 输入语句
	 * @return 事务操作符
	 */
	public static byte translate(String input) {
		if (input.matches("^\\s*(?i)(SHARE\\s+READ)\\s*$")) {
			return RuleOperator.SHARE_READ;
		} else if (input.matches("^\\s*(?i)(SHARE\\s+WRITE)\\s*$")) {
			return RuleOperator.SHARE_WRITE;
		} else if (input.matches("^\\s*(?i)(EXCLUSIVE\\s+WRITE)\\s*$")) {
			return RuleOperator.EXCLUSIVE_WRITE;
		}
		return -1;
	}
}
