/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;

/**
 * <code>SQL FORMAT</code>函数。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/9/2009
 * @since laxcus 1.0
 */
public class Format extends ColumnFunction {

	private static final long serialVersionUID = -1905113172404528788L;

	/**
	 * 构造默认的FORMAT函数
	 */
	public Format() {
		super();
	}

	/**
	 * 生成FORMAT函数数据副本
	 * @param that
	 */
	private Format(Format that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Format duplicate() {
		return new Format(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#matches(java.lang.String)
	 */
	@Override
	public boolean matches(String input) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String sqlText) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

}