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
import com.laxcus.access.type.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL Count</code>函数。统计行或者列的数量，列统计过程中不包含空列(NULL)<br>
 * 
 * @author scott.liang
 * @version 1.0 9/23/2011
 * @since laxcus 1.0
 */
public final class Count extends ColumnAggregateFunction {

	private static final long serialVersionUID = 6616477953261829499L;

	/** COUNT函数正则表达式语句 **/
	private final static String REGEX = "^\\s*(?i)(?:COUNT)\\s*\\(\\s*(\\*|[^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+)\\s*\\)\\s*$";

	/** 如果是SQL语句中的参数是星号(*)，即统计全部行 **/
	private boolean all;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnAggregateFunction#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeBoolean(all);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnAggregateFunction#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		all = reader.readBoolean();
	}
	
	/**
	 * 根据传入的COUNT函数，生成它的副本
	 * @param that
	 */
	private Count(Count that) {
		super(that);
		all = that.all;
	}

	/**
	 * 生成一个默认的统计函数
	 */
	public Count() {
		super();
		all = false;
		// 不支持生成默认列
		setSupportDefault(false);
		// 返回类型固定为INT
		setResultFamily(ColumnType.INTEGER);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Count duplicate() {
		return new Count(this);
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
		// 语法: count(*|column_name)
		Pattern pattern = Pattern.compile(Count.REGEX);
		Matcher matcher = pattern.matcher(primitive);
		if (!matcher.matches()) {
			return null;
		}

		Count count = new Count();
		count.setPrimitive(primitive.trim());
		count.setSpace(table.getSpace());

		String name = matcher.group(1);

		if ("*".equals(name)) {
			count.all = true;
		} else {
			// 查找匹配的列
			ColumnAttribute attribute = table.find(name);
			if (attribute == null) {
				throw new ColumnAttributeException("cannot find '%s'", name);
			}
			// 设置列标识号
			count.setColumnId(attribute.getColumnId());
		}
		return count;
	}

	/**
	 * 统计行或者列的数量
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		int count = 0;
		// 统计行或者统计全部有效的列
		if (all) {
			count = rows.size();
		} else {
			for (Row row : rows) {
				Column column = row.find(getColumnId());
				// 统计有效的列
				if (column != null && !column.isNull()) {
					count++;
				}
			}
		}
		
		// 返回统计结果
		return new com.laxcus.access.column.Integer((short) 0, count);
	}

	/**
	 * COUNT函数不允许产生默认列
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}
}