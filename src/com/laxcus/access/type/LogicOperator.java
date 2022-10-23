/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

import com.laxcus.util.*;

/**
 * 逻辑关系符 <br><br>
 * 
 * SQL检索中，WHERE相邻单元的逻辑连接关系，分为：AND 和 OR两种情况
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public final class LogicOperator {

	/** 无逻辑关系。默认形态 **/
	public final static byte NONE = 0;

	/** AND 关系 **/
	public final static byte AND = 1;

	/** OR 关系 **/
	public final static byte OR = 2;

	/**
	 * 判断是逻辑关系
	 * @param who  逻辑关系符
	 * @return  返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case LogicOperator.NONE:
		case LogicOperator.AND:
		case LogicOperator.OR:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是"与"逻辑关系
	 * @return 返回真或者假
	 */
	public static boolean isAND(byte who) {
		return who == LogicOperator.AND;
	}

	/**
	 * 判断是"或"逻辑关系
	 * @return 返回真或者假
	 */
	public static boolean isOR(byte who) {
		return who == LogicOperator.OR;
	}

	/**
	 * 判断无逻辑联系
	 * @return 返回真或者假
	 */
	public static boolean isNone(byte who) {
		return who == LogicOperator.NONE;
	}

	/**
	 * 数字转为字符串描述
	 * @param who  数据描述
	 * @return  字符串描述
	 */
	public static String translate(byte who) {
		switch(who) {
		case LogicOperator.AND:
			return "AND";
		case LogicOperator.OR:
			return "OR";
		case LogicOperator.NONE:
			return "NONE";
		}
		
		throw new IllegalValueException("illegal logic operator: %d", who);
	}

	/**
	 * 逻辑字符串转为数字描述
	 * @param who  字节串
	 * @return  数字描述
	 */
	public static byte translate(String who) {
		if (who.matches("^\\s*(?i)(?:AND)\\s*$")) {
			return LogicOperator.AND;
		} else if (who.matches("^\\s*(?i)(?:OR)\\s*$")) {
			return LogicOperator.OR;
		} else if (who.matches("^\\s*(?i)(?:NONE)\\s*$")) {
			return LogicOperator.NONE;
		}

		throw new IllegalValueException("illegal logic operator: %s", who);
	}
}
