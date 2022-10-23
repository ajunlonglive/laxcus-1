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
 * <code>SQL MAX</code>函数，它是聚合函数中的一种。<br>
 * 
 * MAX 函数计算一组列中的最大值，NULL值不包括在计算内。<br>
 * MIN 和 MAX 也可用于文本列，以获得按字母顺序排列的最高或最低值。<br>
 * 
 * @author scott.liang
 * @version 1.0 1/23/2012
 * @since laxcus 1.0
 */
public final class Max extends ColumnAggregateFunction {

	private static final long serialVersionUID = 3718243179660306710L;
	
	/** MAX正则表达式 **/
	private final static String REGEX = "^\\s*(?i)(?:MAX)\\s*\\(\\s*([^\\p{Cntrl}^\\p{Space}^\\p{Punct}[\\_]]+?)\\s*\\)\\s*$";
	
	/**
	 * 生成一个默认的MAX函数
	 */
	public Max() {
		super();
		// 不支持产生默认列
		setSupportDefault(false);
	}

	/**
	 * 根据传入的MAX函数，生成它的副本
	 * @param that
	 */
	private Max(Max that) {
		super(that);
	}

	/**
	 * 根据当前MAX函数，生成它的副本
	 * @see com.laxcus.access.function.Function#duplicate()
	 */
	@Override
	public Max duplicate() {
		return new Max(this);
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
	 * 根据数据表和SQL语句，生成一个MAX函数
	 * @see com.laxcus.access.function.table.ColumnFunction#create(com.laxcus.access.schema.Table, java.lang.String)
	 */
	@Override
	public ColumnFunction create(Table table, String input) {
		// 语法: MAX(column_name)
		Pattern pattern = Pattern.compile(Max.REGEX);
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

		// MAX可以计算任何列的最大值
		Max max = new Max();
		max.setPrimitive(input.trim());
		max.setSpace(table.getSpace());
		max.setColumnId(attribute.getColumnId());
		max.setResultFamily(attribute.getType());

		// 如果是可变长数组类型，设置封闭配置接口
		if (attribute.isVariable()) {
			max.setPacking(((VariableAttribute) attribute).getPacking());
		}
		// 如果是字符类型，设置大小写敏感检查
		if (attribute.isWord()) {
			max.setSentient(((WordAttribute) attribute).isSentient());
		}

		return max;
	}

	/**
	 * 按照字节顺序，选择最大的那个二进制数组列
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
	 * 按照字符字典排序，选择最大的那个字符列
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
	 * 比较数值类型的列
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
			} else if(column.compare(result) > 0) {
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
		
		System.out.printf("column id is:%d\n", super.getColumnId());
		
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
	 * 不产生默认列
	 * @see com.laxcus.access.function.table.ColumnFunction#getDefault()
	 */
	@Override
	public Column getDefault() {
		return null;
	}
}