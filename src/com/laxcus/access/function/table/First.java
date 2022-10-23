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
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;

/**
 * <code>SQL FIRST</code>函数。<br>
 * 从集合中取出第一行的某一列参数。
 * 
 * @author scott.liang
 * @version 1.1 12/9/2009
 * @since laxcus 1.0
 */
public final class First extends ColumnAggregateFunction {

	private static final long serialVersionUID = -6502558348524338023L;
	
	/** FIRST正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:FIRST)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)\\s*\\)\\s*$";

	/**
	 * 根据传入的参数，生成它的副本
	 * @param that
	 */
	private First(First that) {
		super(that);
	}

	/**
	 * 构造一个FIRST函数
	 */
	public First() {
		super();
		// 不产生默认的列
		super.setSupportDefault(false);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public First duplicate() {
		return new First(this);
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String primitive) {
		// 语法: FIRST(column_name)
		Pattern pattern = Pattern.compile(First.REGEX);
		Matcher matcher = pattern.matcher(primitive);
		if (!matcher.matches()) {
			return null;
		}
		String name = matcher.group(1);

		// 查找匹配的列
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throw new ColumnAttributeException("cannot find '%s'", name);
		}

		First first = new First();
		first.setPrimitive(primitive.trim());
		first.setSpace(table.getSpace());
		first.setColumnId(attribute.getColumnId());
		first.setResultFamily(attribute.getType());

		return first;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		// 只能传入一行记录
		if(rows.size() < 1) {
			return null;
		}

		// 取第一行的某一列
		Row row = rows.get(0);
		Column column = row.find(getColumnId());
		return column;
	}

	/**
	 * 不产生默认值
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}

}