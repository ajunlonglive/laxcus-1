/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.sort;

import com.laxcus.access.column.*;

/**
 * <code>SQL "GROUP BY"</code>的分组键。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/18/2011
 * @since laxcus 1.0
 */
final class GroupKey {

	/** GROUP BY中的指定的列 **/
	private Column[] keys;

	/** 散列值 **/
	private int hash;

	/**
	 * 设置"GROUP BY"的分组列值
	 * @param keys 列数组
	 */
	public GroupKey(Column[] keys) {
		super();
		this.setKeys(keys);
	}
	
	/**
	 * 设置列值集合
	 * @param a 列数组
	 */
	public void setKeys(Column[] a) {
		this.keys = a;
	}

	/**
	 * 返回列值集合
	 * @return 列数组
	 */
	public Column[] getKeys() {
		return this.keys;
	}

	/**
	 * 比较参数值是否一致
	 * @param that GroupKey实例
	 * @return 返回真或者假
	 */
	private boolean equals(GroupKey that) {
		if (keys.length != that.keys.length) {
			return false;
		}
		for (int i = 0; i < keys.length; i++) {
			if (!keys[i].equals(that.keys[i])) return false;
		}
		return true;
	}

	/*
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != GroupKey.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return this.equals((GroupKey) that);
	}

	/*
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.hash == 0 && keys != null) {
			if (keys.length > 0) {
				this.hash = keys[0].hashCode();
			}
			for (int i = 1; i < keys.length; i++) {
				this.hash ^= keys[i].hashCode();
			}
		}
		return this.hash;
	}

//	/* (non-Javadoc)
//	 * @see java.lang.Comparable#compareTo(java.lang.Object)
//	 */
//	@Override
//	public int compareTo(GroupKey key) {
//		if (columns.length < key.columns.length) return -1;
//		else if (columns.length > key.columns.length) return 1;
//
//		// 在这里调用ColumnEqualtor(里面处理大小写敏感，解包等)，然后逐一比较各列
//		GroupKeyComparator equaltor = new GroupKeyComparator(null);
//		
//		return equaltor.comparate(columns, key.columns);
//		
////		for (int i = 0; i < columns.length; i++) {
////			int ret = columns[i].compareTo(key.columns[i]);
////			if (ret != 0) return ret;
////		}
////		return 0;
//		
//	}

}