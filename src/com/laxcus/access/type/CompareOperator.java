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
 * 比较关系符
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public final class CompareOperator {

	/** SQL检索时，列与列，列与函数，列与数值之间的比较关系 **/

	/** 一般WHERE检索比较符 **/
	public final static byte EQUAL = 1;
	public final static byte NOT_EQUAL = 2;
	public final static byte LESS = 3;
	public final static byte LESS_EQUAL = 4;
	public final static byte GREATER = 5;
	public final static byte GREATER_EQUAL = 6;

	public final static byte LIKE = 7;
	public final static byte IS_NULL = 8;
	public final static byte NOT_NULL = 9;
	public final static byte IS_EMPTY = 10;
	public final static byte NOT_EMPTY = 11;
	
	/** 专门用于提取一个数据块的全部数据 **/
	public final static byte ALL = 12;

	/**
	 * 判断支持底层的JNI比较
	 * @param who 比较符号
	 * @return 返回真或者假
	 */
	public static boolean isSupportJNI(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.LESS:
		case CompareOperator.LESS_EQUAL:
		case CompareOperator.GREATER:
		case CompareOperator.GREATER_EQUAL:
		case CompareOperator.LIKE:
		case CompareOperator.IS_NULL:
		case CompareOperator.NOT_NULL:
		case CompareOperator.IS_EMPTY:
		case CompareOperator.NOT_EMPTY:
		case CompareOperator.ALL:
			return true;
		default:
			return false;
		}
	}
	
	/** IN/NOT IN语句用于嵌套检索和一般检索 **/
	public final static byte IN = 21; 
	public final static byte NOT_IN = 22;

	/** EXISTS/NOT EXISTS语句用于嵌套检索 **/
	public final static byte EXISTS = 23;
	public final static byte NOT_EXISTS = 24;
	
	/** 查询全部，用于嵌套查询 **/
	public final static byte EQUAL_ALL = 25;
	public final static byte NOTEQUAL_ALL = 27;
	public final static byte LESS_ALL = 29;
	public final static byte GREATER_ALL = 31;
	public final static byte LESS_EQUAL_ALL = 33;
	public final static byte GREATER_EQUAL_ALL = 35;
	
	/** 查询任意一个，用于嵌套查询 **/
	public final static byte EQUAL_ANY = 26;
	public final static byte NOTEQUAL_ANY = 28;
	public final static byte LESS_ANY = 30;
	public final static byte GREATER_ANY = 32;
	public final static byte LESS_EQUAL_ANY = 34;
	public final static byte GREATER_EQUAL_ANY = 36;

	/**
	 * 判断是比较关系符
	 * @param who 关系符
	 * @return 匹配返回真，否则假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.LESS:
		case CompareOperator.LESS_EQUAL:
		case CompareOperator.GREATER:
		case CompareOperator.GREATER_EQUAL:
		case CompareOperator.LIKE:
		case CompareOperator.IS_NULL:
		case CompareOperator.NOT_NULL:
		case CompareOperator.IS_EMPTY:
		case CompareOperator.NOT_EMPTY:
		case CompareOperator.ALL:
			// 子查询IN | NOT IN语句
		case CompareOperator.IN:
		case CompareOperator.NOT_IN:
			// 子检索EXISTS|NOT EXISTS语句
		case CompareOperator.EXISTS:
		case CompareOperator.NOT_EXISTS:
			// 子检索ALL语句
		case CompareOperator.EQUAL_ALL:
		case CompareOperator.NOTEQUAL_ALL:
		case CompareOperator.LESS_ALL:
		case CompareOperator.GREATER_ALL:
		case CompareOperator.LESS_EQUAL_ALL:
		case CompareOperator.GREATER_EQUAL_ALL:
			// 子查询ANY语句
		case CompareOperator.EQUAL_ANY:
		case CompareOperator.NOTEQUAL_ANY:
		case CompareOperator.LESS_ANY:
		case CompareOperator.GREATER_ANY:
		case CompareOperator.LESS_EQUAL_ANY:	
		case CompareOperator.GREATER_EQUAL_ANY:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是匹配的日期操作符
	 * @param who 数字相关操作符
	 * @return 匹配返回真，否则假
	 */
	public static boolean isCalindaOperator(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.LESS:
		case CompareOperator.LESS_EQUAL:
		case CompareOperator.GREATER:
		case CompareOperator.GREATER_EQUAL:
		case CompareOperator.IS_NULL:
		case CompareOperator.NOT_NULL:
			return true;
		}
		return false;
	}
	
	/**
	 * 判断是匹配的数字操作符
	 * @param who 数字相关操作符
	 * @return 匹配返回真，否则假
	 */
	public static boolean isNumberOperator(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.LESS:
		case CompareOperator.LESS_EQUAL:
		case CompareOperator.GREATER:
		case CompareOperator.GREATER_EQUAL:
		case CompareOperator.IS_NULL:
		case CompareOperator.NOT_NULL:
			return true;
		}
		return false;
	}
	
	/**
	 * 判断是匹配的媒体操作符
	 * @param who 字符串相关操作符
	 * @return 匹配返回真，否则假
	 */
	public static boolean isMediaOperator(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.IS_NULL:
		case CompareOperator.NOT_NULL:
		case CompareOperator.IS_EMPTY:
		case CompareOperator.NOT_EMPTY:
			return true;
		}
		return false;
	}

	/**
	 * 判断是匹配的二进制字节数组操作符
	 * @param who 字符串相关操作符
	 * @return 匹配返回真，否则假
	 */
	public static boolean isRawOperator(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.IS_NULL:
		case CompareOperator.NOT_NULL:
		case CompareOperator.IS_EMPTY:
		case CompareOperator.NOT_EMPTY:
			return true;
		}
		return false;
	}

	/**
	 * 判断是匹配的字符串操作符
	 * @param who 字符串相关操作符
	 * @return 匹配返回真，否则假
	 */
	public static boolean isWordOperator(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
		case CompareOperator.NOT_EQUAL:
		case CompareOperator.IS_NULL:
		case CompareOperator.NOT_NULL:
		case CompareOperator.IS_EMPTY:
		case CompareOperator.NOT_EMPTY:
		case CompareOperator.LIKE:
			return true;
		}
		return false;
	}

	/**
	 * 字符串翻译为数字描述
	 * @param who  比较关系符的字符串描述
	 * @return  数字描述
	 */
	public static byte translate(String who) {		
		if (who.matches("^\\s*(?i)(?:>)\\s*$")) {
			return CompareOperator.GREATER;
		} else if (who.matches("^\\s*(?i)(?:>=)\\s*$")) {
			return CompareOperator.GREATER_EQUAL;
		} else if (who.matches("^\\s*(?i)(?:<)\\s*$")) {
			return CompareOperator.LESS;
		} else if (who.matches("^\\s*(?i)(?:<=)\\s*$")) {
			return CompareOperator.LESS_EQUAL;
		} else if (who.matches("^\\s*(?i)(?:=)\\s*$")) {
			return CompareOperator.EQUAL;
		} else if (who.matches("^\\s*(?i)(?:<>|!=)\\s*$")) {
			return CompareOperator.NOT_EQUAL;
		} else if (who.matches("^\\s*(?i)LIKE\\s*$")) {
			return CompareOperator.LIKE;
		} else if (who.matches("^\\s*(?i)(IS\\s+NULL)\\s*$")) {
			return CompareOperator.IS_NULL;
		} else if (who.matches("^\\s*(?i)(IS\\s+NOT\\s+NULL)\\s*$")) {
			return CompareOperator.NOT_NULL;
		} else if (who.matches("^\\s*(?i)(IS\\s+EMPTY)\\s*$")) {
			return CompareOperator.IS_EMPTY;
		} else if (who.matches("^\\s*(?i)(IS\\s+NOT\\s+EMPTY)\\s*$")) {
			return CompareOperator.NOT_EMPTY;
		} else if (who.matches("^\\s*(?i)ALL\\s*$")) {
			return CompareOperator.ALL;
		} else if (who.matches("^\\s*(?i)IN\\s*$")) {
			return CompareOperator.IN;
		} else if (who.matches("^\\s*(?i)(NOT\\s+IN)\\s*$")) {
			return CompareOperator.NOT_IN;
		} else if (who.matches("^\\s*(?i)EXISTS\\s*$")) {
			return CompareOperator.EXISTS;
		} else if (who.matches("^\\s*(?i)(NOT\\s+EXISTS)\\s*$")) {
			return CompareOperator.NOT_EXISTS;
		}
		// 嵌套ALL比较
		else if(who.matches("^\\s*(=)\\s*(?i)(ALL)\\s*$")) {
			return CompareOperator.EQUAL_ALL;
		} else if(who.matches("^\\s*(!=|<>)\\s*(?i)(ALL)\\s*$")) {
			return CompareOperator.NOTEQUAL_ALL;
		} else if(who.matches("^\\s*(>)\\s*(?i)(ALL)\\s*$")) {
			return CompareOperator.GREATER_ALL;
		} else if(who.matches("^\\s*(>=)\\s*(?i)(ALL)\\s*$")) {
			return CompareOperator.GREATER_EQUAL_ALL;
		} else if(who.matches("^\\s*(<)\\s*(?i)(ALL)\\s*$")) {
			return CompareOperator.LESS_ALL;
		} else if(who.matches("^\\s*(<=)\\s*(?i)(ALL)\\s*$")) {
			return CompareOperator.LESS_EQUAL_ALL;
		}
		//  嵌套ANY比较
		else if(who.matches("^\\s*(=)\\s*(?i)(ANY|SOME)\\s*$")) {
			return CompareOperator.EQUAL_ANY;
		} else if(who.matches("^\\s*(!=|<>)\\s*(?i)(ANY|SOME)\\s*$")) {
			return CompareOperator.NOTEQUAL_ANY;
		} else if(who.matches("^\\s*(>)\\s*(?i)(ANY|SOME)\\s*$")) {
			return CompareOperator.GREATER_ANY;
		} else if(who.matches("^\\s*(>=)\\s*(?i)(ANY|SOME)\\s*$")) {
			return CompareOperator.GREATER_EQUAL_ANY;
		} else if(who.matches("^\\s*(<)\\s*(?i)(ANY|SOME)\\s*$")) {
			return CompareOperator.LESS_ANY;
		} else if(who.matches("^\\s*(<=)\\s*(?i)(ANY|SOME)\\s*$")) {
			return CompareOperator.LESS_EQUAL_ANY;
		}

		throw new IllegalValueException("invalid compare operator:%s", who);
	}

	/**
	 * 比较关系符翻译为字符串描述 
	 * @param who  比较关系符
	 * @return  字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case CompareOperator.EQUAL:
			return "=";
		case CompareOperator.NOT_EQUAL:
			return "<>";
		case CompareOperator.LESS:
			return "<";
		case CompareOperator.LESS_EQUAL:
			return "<=";
		case CompareOperator.GREATER:
			return ">";
		case CompareOperator.GREATER_EQUAL:
			return ">=";
		case CompareOperator.LIKE:
			return "LIKE";
		case CompareOperator.IS_NULL:
			return "IS NULL";
		case CompareOperator.NOT_NULL:
			return "IS NOT NULL";
		case CompareOperator.IS_EMPTY:
			return "IS EMPTY";
		case CompareOperator.NOT_EMPTY:
			return "IS NOT EMPTY";
		case CompareOperator.ALL:
			return "ALL";
		case CompareOperator.IN:
			return "IN";
		case CompareOperator.NOT_IN:
			return "NOT IN";
		case CompareOperator.EXISTS:
			return "EXISTS";
		case CompareOperator.NOT_EXISTS:
			return "NOT EXISTS";

			// 全部
		case CompareOperator.EQUAL_ALL:
			return "=ALL";
		case CompareOperator.NOTEQUAL_ALL:
			return "<>ALL";
		case CompareOperator.LESS_ALL:
			return "<ALL";
		case CompareOperator.GREATER_ALL:
			return ">ALL";
		case CompareOperator.LESS_EQUAL_ALL:
			return "<=ALL";
		case CompareOperator.GREATER_EQUAL_ALL:
			return ">=ALL";
			// 任意
		case CompareOperator.EQUAL_ANY:
			return "=ANY";
		case CompareOperator.NOTEQUAL_ANY:
			return "<>ANY";
		case CompareOperator.LESS_ANY:
			return "<ANY";
		case CompareOperator.GREATER_ANY:
			return ">ANY";
		case CompareOperator.LESS_EQUAL_ANY:
			return "<=ANY";
		case CompareOperator.GREATER_EQUAL_ANY:
			return ">=ANY";
		}
		return "";
	}

	/**
	 * IN比较符判断
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isIn(byte who) {
		return who == CompareOperator.IN;
	}

	/**
	 * NOT IN比较符判断
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isNotIn(byte who) {
		return who == CompareOperator.NOT_IN;
	}

	/**
	 * EXISTS比较符判断
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isExists(byte who) {
		return who == CompareOperator.EXISTS;
	}

	/**
	 * NOT EXISTS比较符判断
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isNotExists(byte who) {
		return who == CompareOperator.NOT_EXISTS;
	}

	/**
	 * "=ALL"比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isEqualAll(byte who) {
		return who == CompareOperator.EQUAL_ALL;
	}

	/**
	 * “<>ALL”比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isNotEqualAll(byte who) {
		return who == CompareOperator.NOTEQUAL_ALL;
	}

	/**
	 * “>ALL”比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isGreaterAll(byte who) {
		return who == CompareOperator.GREATER_ALL;
	}

	/**
	 * “<ALL”比较符号
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isLessAll(byte who) {
		return who == CompareOperator.LESS_ALL;
	}
	
	/**
	 * “>=ALL”比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isGreaterEuqlaAll(byte who) {
		return who == CompareOperator.GREATER_EQUAL_ALL;
	}

	/**
	 * “<= ALL”比较符号
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isLessEqualAll(byte who) {
		return who == CompareOperator.LESS_EQUAL_ALL;
	}
	
	/**
	 * “=ANY”比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isEqualAny(byte who) {
		return who == CompareOperator.EQUAL_ANY;
	}

	/**
	 * "<>ANY"比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isNotEqualAny(byte who) {
		return who == CompareOperator.NOTEQUAL_ANY;
	}

	/**
	 * “>ANY”比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isGreaterAny(byte who) {
		return who == CompareOperator.GREATER_ANY;
	}

	/**
	 * “<ANY”比较符号
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isLessAny(byte who) {
		return who == CompareOperator.LESS_ANY;
	}
	
	/**
	 * “>=ANY”比较符
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isGreaterEuqlaAny(byte who) {
		return who == CompareOperator.GREATER_EQUAL_ANY;
	}

	/**
	 * “<= ANY”比较符号
	 * @param who 比较关系符
	 * @return 返回真或者假
	 */
	public static boolean isLessEqualAny(byte who) {
		return who == CompareOperator.LESS_EQUAL_ANY;
	}

}