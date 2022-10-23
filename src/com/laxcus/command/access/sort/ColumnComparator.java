/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.sort;

import com.laxcus.access.column.*;

/**
 * 用户自定义的"列"排序比较器
 * 
 * @author scott.liang
 * @version 1.0 9/30/2009
 * @since laxcus 1.0
 */
public interface ColumnComparator {

	/**
	 * 列编号
	 * @return 列编号的短整型值
	 */
	short getColumnId();
	
	/**
	 * 把两个列值进行比较，默认按照升序排序
	 * @param o1 列1
	 * @param o2 列2
	 * @return 返回比较值
	 */
	int compare(Column o1, Column o2);
	
	/**
	 * 把两个列值进行比较
	 * @param o1 列1
	 * @param o2 列2
	 * @param asc 升序排序或者否
	 * @return 返回比较值
	 */
	int compare(Column o1, Column o2, boolean asc);
}