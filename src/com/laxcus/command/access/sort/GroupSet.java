/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.sort;

import java.util.*;

import com.laxcus.access.row.*;

/**
 * 同质"GROUP BY"的分组键集合。<br>
 * SQL "GROUP BY" 语句的分组存储器，同一KEY值的行记录保存在一起。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/20/2011
 * @since laxcus 1.0
 */
final class GroupSet {

	private ArrayList<Row> array = new ArrayList<Row>();

	/**
	 * 构造"GROUP BY"分组成员集合
	 */
	public GroupSet() {
		super();
	}

	/**
	 * 保存一行
	 * @param row 行
	 * @return 返回真或者假
	 */
	public boolean add(Row row) {
		return array.add(row);
	}

	/**
	 * 输出全部行
	 * @return Row列表
	 */
	public List<Row> list() {
		return this.array;
	}

	/**
	 * 统计行数
	 * @return
	 */
	public int size() {
		return this.array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return this.array.isEmpty();
	}

	/**
	 * 收缩到合适的空间
	 */
	public void trim() {
		this.array.trimToSize();
	}
}