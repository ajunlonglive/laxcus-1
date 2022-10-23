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
 * <code>SQL MIN</code>函数，它属于聚合函数的一种。 <br>
 * 
 * 计算一组列集合中的最小值，NULL值不包括在内。<br>
 * MIN可以判断全部数据类型，包括二进制数组、字符串、数值类型。<br>
 * 
 * @author scott.liang
 * @version 1.0 1/23/2012
 * @since laxcus 1.0
 */
public final class Min extends ColumnAggregateFunction {

	private static final long serialVersionUID = -8188993959531212282L;

	/** MIN正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:MIN)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)\\s*\\)\\s*$";

	/**
	 * 构造一个默认的MIN函数
	 */
	public Min() {
		super();
		// 不能够产生默认列
		setSupportDefault(false);
	}

	/**
	 * 根据传入的MIN函数，生成它的副本
	 * @param that
	 */
	private Min(Min that) {
		super(that);
	}

	/**
	 * 根据当前MIN函数，生成它的副本
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Min duplicate() {
		return new Min(this);
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
	 * 根据传入的数据表和SQL语句，生成一个MIN函数
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String input) {
		// 语法: MIN(column_name)
		Pattern pattern = Pattern.compile(Min.REGEX);
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

		// MIN可以计算任何列的最大值
		Min min = new Min();
		min.setPrimitive(input.trim());
		min.setSpace(table.getSpace());
		min.setColumnId(attribute.getColumnId());
		min.setResultFamily(attribute.getType());

		// 如果是可变长数组类型，设置封闭配置接口
		if (attribute.isVariable()) {
			min.setPacking(((VariableAttribute) attribute).getPacking());
		}
		// 如果是字符类型，设置大小写敏感检查
		if (attribute.isWord()) {
			min.setSentient(((WordAttribute) attribute).isSentient());
		}
		return min;
	}

	/**
	 * 按照字节顺序，选择最小的那个二进制数组列
	 * @param rows
	 * @return
	 */
	private Column choiceRaw(Collection<Row> rows) {
		Packing packing = super.getPacking();
		Column result = null;
		for(Row row : rows) {
			Column column = row.find(getColumnId());
			if(column == null || column.isNull()) continue;
			if (result == null) {
				result = column;
			} else if (((Raw) column).compare((Raw) result, packing, true) > 0) {
				result = column;
			}
		}
		return result;
	}
	
	/**
	 * 按照字符字典排序，选择最小的那个字符列
	 * @param rows
	 * @return
	 */
	private Column choiceWord(Collection<Row> rows) {
		Packing packing = super.getPacking();
		boolean sentient = super.isSentient(); 
		Column result = null;
		for(Row row : rows) {
			Column column = row.find(getColumnId());
			if(column == null || column.isNull()) continue;
			if(result == null) {
				result = column;
			} else if (((Word) column).compare((Word) result, packing, sentient, true) > 0) {
				result = column;
			}
		}		
		return result;
	}
	
	/**
	 * 比较数值类型的列，选择最小的那个列
	 * @param rows
	 * @return
	 */
	private Column choice(Collection<Row> rows) {
		Column result = null;
		for (Row row : rows) {
			Column column = row.find(getColumnId());
			if (column == null || column.isNull()) continue; // 空值忽略
			if(result == null) {
				result = column;
			} else if(column.compare(result) < 0) {
				result = column;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.function.table.ColumnFunction#makeup(java.util.List)
	 */
	@Override
	public Column makeup(List<Row> rows) {
		// 检查列类型是否匹配
		super.check(rows, getColumnId());

		Column result = null;
		if (super.isRaw()) {
			result = this.choiceRaw(rows);
		} else if (super.isWord()) {
			result = this.choiceWord(rows);
		} else {
			result = this.choice(rows);
		}
		return result;
	}

	/**
	 * 不允许产生默认MIN函数
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}

}