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
 * 大字符字符串(UTF32)列比较器
 * 
 * @author scott.liang
 * @version 1.0 11/8/2009
 * @since laxcus 1.0
 */
public class HCharComparator extends WordComparator {

	/**
	 * 构造大字符列比较器
	 */
	public HCharComparator() {
		super(new com.laxcus.util.charset.UTF32());
	}

	/**
	 * 构造大字符列比较器，指定列编号
	 * @param columnId 列编号
	 */
	public HCharComparator(short columnId) {
		this();
		setColumnId(columnId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.sql.method.sort.ColumnComparator#compare(com.laxcus.sql.column.Column, com.laxcus.sql.column.Column)
	 */
	@Override
	public int compare(Column o1, Column o2) {
		return super.compare(((HChar)o1).getValue(), ((HChar)o2).getValue(), true);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.access.sort.ColumnComparator#compare(com.laxcus.access.column.Column, com.laxcus.access.column.Column, boolean)
	 */
	@Override
	public int compare(Column o1, Column o2, boolean asc) {
		return super.compare(((HChar)o1).getValue(), ((HChar)o2).getValue(), asc);
	}

}