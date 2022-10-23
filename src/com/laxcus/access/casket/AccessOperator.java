/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

/**
 * 数据存取操作符
 * 
 * @author scott.liang
 * @version 1.0 11/02/2012
 * @since laxcus 1.0
 */
public final class AccessOperator {

	/** 存取操作符 **/
	public final static byte INSERT = 1;
	public final static byte SELECT = 2;
	public final static byte DELETE = 3;
	public final static byte LEAVE = 4;

	/**
	 * 判断是合法的存取操作符
	 * @param who 存取操作符
	 * @return 返回真或者假
	 */
	public static boolean isOperator(byte who) {
		switch (who) {
		case AccessOperator.INSERT:
		case AccessOperator.SELECT:
		case AccessOperator.DELETE:
		case AccessOperator.LEAVE:
			return true;
		}
		return false;
	}

	/**
	 * 判断是INSERT操作
	 * @param who 存取操作符
	 * @return 返回真或者假
	 */
	public static boolean isInsert(byte who) {
		return who == AccessOperator.INSERT;
	}

	/**
	 * 判断是SELECT操作
	 * @param who 存取操作符
	 * @return 返回真或者假
	 */
	public static boolean isSelect(byte who) {
		return who == AccessOperator.SELECT;
	}

	/**
	 * 判断是DELETE操作
	 * @param who 存取操作符
	 * @return 返回真或者假
	 */
	public static boolean isDelete(byte who) {
		return who == AccessOperator.DELETE;
	}

	/**
	 * 判断是LEAVE操作
	 * @param who 存取操作符
	 * @return 返回真或者假
	 */
	public static boolean isLeave(byte who) {
		return who == AccessOperator.LEAVE;
	}
}