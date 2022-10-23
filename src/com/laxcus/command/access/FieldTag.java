/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

/**
 * 数据库操作方法的域单元标记
 * 
 * @author scott.liang
 * @version 1.3 1/23/2012
 * @since laxcus 1.0
 */
public final class FieldTag {

	/** SQL语句操作单元编号 */
	public final static byte SPACE = 1;		// 数据表名
	public final static byte CONDITION = 2;	// WHERE检索条件
	public final static byte COLUMNIDS = 3; // 显示列成员

	public final static byte ORDERBY = 5;
	public final static byte RANGE = 6;
//	public final static byte SNATCH = 7;	// DELETE SNATCH，是否获取删除数据
	public final static byte SDOTSET = 8;	
	public final static byte LISTSHEET = 9;
	public final static byte GROUPBY = 10;
	public final static byte UPDATE_SETS = 11;
	public final static byte DISTINCT = 12;

	
	/**
	 * 判断是合法的域单元标记
	 * @param who 域单元标记
	 * @return 返回真或者假
	 */
	public static boolean isField(byte who) {
		switch (who) {
		case FieldTag.SPACE:
		case FieldTag.CONDITION:
		case FieldTag.COLUMNIDS:
		case FieldTag.ORDERBY:
		case FieldTag.RANGE:
//		case FieldTag.SNATCH:
		case FieldTag.SDOTSET:
		case FieldTag.LISTSHEET:
		case FieldTag.GROUPBY:
		case FieldTag.UPDATE_SETS:
		case FieldTag.DISTINCT:
			return true;
		}
		return false;
	}

}