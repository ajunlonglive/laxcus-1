/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index;

import com.laxcus.access.column.*;

/**
 * 列索引 <br><br>
 * 
 * <code>SQL WHERE</code>检索时的校验参数。<br>
 * 格式如: WHERE column_name (>|<|>=|<=|=) value <br>
 * 
 * @author scott.liang
 * @version 1.0 4/19/2009
 * @since laxcus 1.0
 */
public abstract class ColumnIndex extends WhereIndex {

	private static final long serialVersionUID = 3854003142653357920L;

	/** 被检索列 **/
	private Column column;
	
	/**
	 * 构造WHERE检索参数，并且指定它的检索索引类型
	 * @param family 检索索引类型
	 */
	protected ColumnIndex(byte family) {
		super(family);
	}

	/**
	 * 根据传入参数，生成WHERE校验参数的副本
	 * @param that ColumnIndex实例
	 */
	protected ColumnIndex(ColumnIndex that) {
		super(that);
		if (that.column != null) {
			column = that.column.duplicate();
		}
	}

	/**
	 * 设置检索列
	 * @param e Column实例
	 */
	public void setColumn(Column e) {
		column = e;
	}

	/**
	 * 返回检索列参数
	 * @return Column实例
	 */
	public Column getColumn() {
		return column;
	}

	/**
	 * 设置列编号
	 * @see com.laxcus.access.index.WhereIndex#setColumnId(short)
	 */
	@Override
	public void setColumnId(short id) {
		if (column != null) {
			column.setId(id);
		}
	}

	/**
	 * 返回列编号。如果列定义返回列编号，如果没有返回0
	 * @see com.laxcus.access.index.WhereIndex#getColumnId()
	 */
	@Override
	public short getColumnId() {
		if (column != null) {
			return column.getId();
		}
		return 0;
	}

}