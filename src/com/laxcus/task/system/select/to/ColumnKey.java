/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.util.*;

/**
 * 列键值，包括列和它对应的属性
 * 
 * @author scott.liang
 * @version 1.0 3/23/2014
 * @since laxcus 1.0
 */
final class ColumnKey implements Comparable<ColumnKey> {

	/** 列值 **/
	private Column column;

	/** 列属性 **/
	private ColumnAttribute attribute;

	/**
	 * 
	 */
	public ColumnKey(Column s1, ColumnAttribute s2) {
		this.column = s1;
		this.attribute = s2;
	}

	/**
	 * 返回列
	 * @return
	 */
	public Column getColumn() {
		return this.column;
	}

	/**
	 * 返回它的属性
	 * @return
	 */
	public ColumnAttribute getAttribute() {
		return this.attribute;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ColumnKey.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return this.compareTo((ColumnKey) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return column.hashCode() ^ attribute.hashCode();
	}
	
	/**
	 * 根据列的参数值，比较它们是否一致
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ColumnKey that) {
		if (that == null) {
			return 1;
		}

		if (column.getType() != that.column.getType()) {
			throw new java.lang.ClassCastException();
		}

		// 如果是二进制数组时
		if (column.isRaw()) {
			Packing p1 = ((VariableAttribute) attribute).getPacking();
			Packing p2 = ((VariableAttribute) that.attribute).getPacking();
			byte[] b1 = ((Raw) column).getValue(p1);
			byte[] b2 = ((Raw) that.column).getValue(p2);
			return Laxkit.compareTo(b1, b2);
		} else if (column.isWord()) {
			// 如果是字符数组时
			Packing p1 = ((WordAttribute) attribute).getPacking();
			Packing p2 = ((WordAttribute) that.attribute).getPacking();
			// 只要有一个忽略大小写，就以它为准
			boolean ignore = (!((WordAttribute) attribute).isSentient() || 
					!((WordAttribute) that.attribute).isSentient());
			
			String s1 = ((Word) column).toString(p1);
			String s2 = ((Word) that.column).toString(p2);
			if (ignore) {
				return s1.compareToIgnoreCase(s2);
			} else {
				return s1.compareTo(s2);
			}
		} else {
			// 使用默认的值
			return column.compare(that.column);
		}
	}

}