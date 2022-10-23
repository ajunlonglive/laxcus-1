/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import java.util.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.datetime.*;

/**
 * 当前日期函数。返回一个时间值，格式：年-月-日。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/23/2009
 * @since laxcus 1.0
 */
public final class Today extends ColumnFunction {

	private static final long serialVersionUID = 7012940610356629296L;
	
	/** TODAY函数正则表达式 **/
	private final static String REGEX = "^\\s*(?i)TODAY\\s*\\(\\s*\\)\\s*$";
	
	/**
	 * 构造TODAY函数
	 */
	public Today() {
		super(ColumnType.DATE);
		// 允许生成默认列
		super.setSupportDefault(true);
	}

	/**
	 * 生成TODAY函数的数据副本
	 * @param that
	 */
	private Today(Today that) {
		super(that);
	}

	/**
	 * 生成TODAY函数副本
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Today duplicate() {
		return new Today(this);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#matches(java.lang.String)
	 */
	@Override
	public boolean matches(String input) {
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 生成TODAY实例
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String input) {
		Pattern pattern = Pattern.compile(Today.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}
		// 生成函数
		Today function = new Today();
		function.setPrimitive(input.trim());
		return function;
	}

	/**
	 * 计算当前系统日期
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		return getDefault();
	}

	/**
	 * 生成一个默认日期列
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		com.laxcus.access.column.Date d = new com.laxcus.access.column.Date();
		int value = SimpleDate.format(new java.util.Date());
		d.setValue(value);
		return d;
	}

}