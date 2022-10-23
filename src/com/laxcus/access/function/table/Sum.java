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
 * SUM函数 <br>
 * 属于聚合函数，统计列参数总量。空值(NULL)不在统计范围内。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/23/2009
 * @since laxcus 1.0
 */
public final class Sum extends ColumnAggregateFunction {

	private static final long serialVersionUID = 2899705611729935880L;

	/** SUM函数 正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:SUM)\\s*\\(\\s*(.+?)\\s*\\)\\s*$";

	/**
	 * 构造默认的SUM函数
	 */
	public Sum() {
		super();
	}

	/**
	 * 生成SUM函数的数据副本
	 * @param that
	 */
	private Sum(Sum that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Sum duplicate() {
		return new Sum(this);
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
	 * 根据传入参数生成SUM对象实例
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String input) {
		// 语法: SUM(column_name)
		Pattern pattern = Pattern.compile(Sum.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}
		String name = matcher.group(1);

		// 查找匹配的列
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throw new ColumnAttributeException("cannot find \'%s\'", name);
		}
		// 必须是数值类型的列
		if (!attribute.isNumber()) {
			throw new ColumnAttributeException("cannot support %s", name);
		}
		
		Sum sum = new Sum();
		sum.setPrimitive(input.trim());
		sum.setColumnId(attribute.getColumnId());
		sum.setResultFamily(attribute.getType());
		sum.setSpace(table.getSpace());

		return sum;
	}

	/**
	 * 统计结果值
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		Column result = null;
		
		// 检查类型
		for (Row row : rows) {
			Column rs = row.find(super.getColumnId());
			if (rs.getType() != this.getResultFamily()) {
				throw new IllegalArgumentException("not match type");
			}
		}
		
		// 计算结果
		if(this.isShort()) {
			short count = 0;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				short num = ((com.laxcus.access.column.Short) column).getValue();
				count += num;
			}
			result = new com.laxcus.access.column.Short((short)0, count);
		} else if( this.isInteger()) {
			int count = 0;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				int num = ((com.laxcus.access.column.Integer)column).getValue();
				count += num;
			}
			result = new com.laxcus.access.column.Integer((short) 0, count);
		} else if(this.isLong()) {
			long count = 0;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				long num = ((com.laxcus.access.column.Long) column).getValue();
				count += num;
			}
			result = new com.laxcus.access.column.Long((short) 0, count);
		} else if(this.isFloat()) {
			float count = 0.0f;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				float num = ((com.laxcus.access.column.Float) column).getValue();
				count+= num;
			}
			result = new com.laxcus.access.column.Float((short)0, count);
		} else if(this.isDouble()) {
			double count = 0.0f;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				double num = ((com.laxcus.access.column.Double) column).getValue();
				count += num;
			}
			result = new com.laxcus.access.column.Double((short)0, count);
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}
	
}