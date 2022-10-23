/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

/**
 * SQL操作命令标识
 * 
 * @author scott.liang
 * @version 1.0 1/23/2009
 * @since laxcus 1.0
 */
public final class SQLTag {

	/** SELECT命令 */
	public final static byte SELECT_METHOD = 1;

	/** DELETE命令 **/
	public final static byte DELETE_METHOD = 2;

	/** UPDATE命令 **/
	public final static byte UPDATE_METHOD = 3;

	/** INSERT命令 **/
	public final static byte INSERT_METHOD = 4;

	/** JOIN命令 **/
	public final static byte JOIN_METHOD = 6;
	
	/** 基于SELECT插入之上的INJECT命令 **/
	public final static byte INJECT_SELECT_METHOD = 7;
	
	/**
	 * 判断是SQL操作命令
	 * @param who SQL操作命令
	 * @return 返回真或者假
	 */
	public static boolean isMethod(byte who) {
		switch (who) {
		case SQLTag.SELECT_METHOD:
		case SQLTag.DELETE_METHOD:
		case SQLTag.UPDATE_METHOD:
		case SQLTag.INSERT_METHOD:
		case SQLTag.JOIN_METHOD:
		case SQLTag.INJECT_SELECT_METHOD:
			return true;
		}
		return false;
	}

}