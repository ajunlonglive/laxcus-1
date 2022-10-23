/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

/**
 * 权限表类型
 * 
 * @author scott.liang
 * @version 1.0 7/17/2009
 * @since laxcus 1.0
 */
public final class PermitTag {

	/** 用户级权限 **/
	public final static int USER_PERMIT = 1;

	/** 数据库级权限 **/
	public final static int SCHEMA_PERMIT = 2;

	/** 表极权限 **/
	public final static int TABLE_PERMIT = 3;

	/**
	 * 判断是合法的权限表类型
	 * @param who  权限类型
	 * @return  返回真或者假
	 */
	public static boolean isFamily(int who) {
		switch (who) {
		case PermitTag.USER_PERMIT:
		case PermitTag.SCHEMA_PERMIT:
		case PermitTag.TABLE_PERMIT:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 判断是用户级操作权限
	 * @return 返回真或者假
	 */
	public static boolean isUserPermit(int who) {
		return who == PermitTag.USER_PERMIT;
	}

	/**
	 * 判断是数据库级操作权限
	 * @return 返回真或者假
	 */
	public static boolean isSchemaPrimit(int who) {
		return who == PermitTag.SCHEMA_PERMIT;
	}

	/**
	 * 判断是数据库表级操作权限
	 * @return 返回真或者假
	 */
	public static boolean isTablePermit(int who) {
		return who == PermitTag.TABLE_PERMIT;
	}
	
	/**
	 * 翻译权限标记描述
	 * @param input 权限标记描述
	 * @return  返回数字描述
	 */
	public static int translate(String input) {
		if (input.matches("^\\s*(?i)(?:USER)\\s*$")) {
			return PermitTag.USER_PERMIT;
		} else if (input.matches("^\\s*(?i)(?:SCHEMA)\\s*$")) {
			return PermitTag.SCHEMA_PERMIT;
		} else if (input.matches("^\\s*(?i)(?:TABLE)\\s*$")) {
			return PermitTag.TABLE_PERMIT;
		}
		return -1;
	}

	/**
	 * 翻译权限标记描述
	 * @param who 权限类型
	 * @return  返回文本描述
	 */
	public static String translate(int who) {
		switch (who) {
		case PermitTag.USER_PERMIT:
			return "USER";
		case PermitTag.SCHEMA_PERMIT:
			return "SCHEMA";
		case PermitTag.TABLE_PERMIT:
			return "TABLE";
		}
		return "UNKNOWN";
	}

}