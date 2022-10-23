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
 * <code>SQL AVG</code>函数。<br>
 * 计算一组数值列的平均值，对应列必须是数值类型，NULL值不在计算范围内。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/19/2009
 * @since laxcus 1.0
 */
public final class Avg extends ColumnAggregateFunction {
	
	private static final long serialVersionUID = -6539926250286528309L;
	
	/** AVG正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:AVG)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)\\s*\\)\\s*$";
	
	/**
	 * 生成一个默认的计算平均值函数
	 */
	public Avg() {
		super();
		// 不支持产生默认列
		super.setSupportDefault(false);
	}

	/**
	 * 根据传入的AVG函数，生成它的副本
	 * @param that Avg实例
	 */
	private Avg(Avg that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Avg duplicate() {
		return new Avg(this);
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
	public ColumnFunction create(Table table, String input) {
		// 语法: AVG(column_name)
		Pattern pattern = Pattern.compile(Avg.REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			return null;
		}
		String name = matcher.group(1);

		// 查找匹配的列
		ColumnAttribute attribute = table.find(name);
		if (attribute == null) {
			throw new ColumnAttributeException("cannot find '%s'", name);
		}
		// 必须是数值类型的列
		if(!attribute.isNumber()) {
			throw new ColumnAttributeException("cannot support '%s'", name);
		}

		Avg avg = new Avg();
		avg.setPrimitive(input.trim());
		avg.setSpace(table.getSpace());
		avg.setColumnId(attribute.getColumnId());
		avg.setResultFamily(attribute.getType());

		return avg;
	}

	/**
	 * 计算平均值，被计算的列必须是数值型
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		Column result = null;
		
		// 检查列类型是否匹配
		for(Row row : rows) {
			Column rs = row.find( super.getColumnId());
			if(rs.getType() != this.getResultFamily()) {
				throw new IllegalArgumentException("not match type");
			}
		}
		
		int count = 0;
		
		if(this.isShort()) {
			short value = 0;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				short num = ((com.laxcus.access.column.Short) column).getValue();
				value += num;
				count++;
			}
			result = new com.laxcus.access.column.Short((short) 0, (short) (value / count));
		} else if(this.isInteger()) {
			int value = 0;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				int num = ((com.laxcus.access.column.Integer) column).getValue();
				value += num;
				count++;
			}
			result = new com.laxcus.access.column.Integer((short) 0, value / count);
		} else if(super.isLong()) {
			long value = 0L;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				long num = ((com.laxcus.access.column.Long) column).getValue();
				value += num;
				count++;
			}
			result = new com.laxcus.access.column.Long((short) 0, value / count);
		} else if(super.isFloat()) {
			float value = 0.0f;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				float num = ((com.laxcus.access.column.Float) column).getValue();
				value += num;
				count++;
			}
			result = new com.laxcus.access.column.Float((short) 0, value / count);
		} else if(super.isDouble()) {
			double value = 0.0f;
			for(Row row : rows) {
				Column column = row.find(getColumnId());
				if (column == null || column.isNull()) continue;
				double num = ((com.laxcus.access.column.Double) column).getValue();
				value += num;
				count++;
			}
			result = new com.laxcus.access.column.Double((short) 0, value / count);
		}
		
		return result;
	}

	/**
	 * 不支持产生默认列
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}



}
