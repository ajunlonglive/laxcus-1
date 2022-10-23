/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.sort;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.row.*;
import com.laxcus.command.access.select.*;

/**
 * <code>SQL "ORDER BY"</code>排序器。<br>
 * 根据两个记录同一列，比较然后排列两行记录的先后顺序。<br><br>
 * 
 * 调用此类接口是: java.util.Collections.sort(T[] a, java.util.Comparator<? supert T> c) <br>
 * 
 * @author scott.liang
 * @version 1.0 11/13/2011
 * @since laxcus 1.0
 */
public class OrderBySorter implements Comparator<Row> {

	/** "ORDER BY" 实例 **/
	private OrderByAdapter adapter;
	
	/** 列标识->列自定义比较器 (区别与Column中的标准比较，这里用户自定义的比较，如字符和二进制的比较，用户需自定义类实现) **/
	private TreeMap<java.lang.Short, ColumnComparator> set = new TreeMap<java.lang.Short, ColumnComparator>();
	
	/**
	 * 构造默认的ORDER BY排序器
	 */
	public OrderBySorter() {
		super();
	}

	/**
	 * 构造ORDER BY排序器，指定ORDER BY适配器
	 * @param e OrderByAdapter实例
	 */
	public OrderBySorter(OrderByAdapter e) {
		this();
		this.setAdapter(e);
	}
	
	/**
	 * 设置 "ORDER BY"适配器
	 * @param e OrderByAdapter实例
	 */
	public void setAdapter(OrderByAdapter e) {
		this.adapter = e;
	}

	/**
	 * 返回"ORDER BY"适配器
	 * @return OrderByAdapter实例
	 */
	public OrderByAdapter getAdapter() {
		return this.adapter;
	}

	/**
	 * 保存一个列比较器
	 * @param comparator 列比较器
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ColumnComparator comparator) {
		short columnId = comparator.getColumnId();
		return set.put(columnId, comparator) == null;
	}

	/**
	 * 根据列编号，删除一个列比较器
	 * 
	 * @param columnId 列编号
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(short columnId) {
		return set.remove(columnId) != null;
	}
	
	/**
	 * 根据列编号，查找对应的列比较器
	 * @param columnId 列编号
	 * @return 返回ColumnComparator实例
	 */
	public ColumnComparator get(short columnId){
		return set.get(columnId);
	}
	
	/**
	 * 返回列编号集合
	 * @return short集合
	 */
	public java.util.Set<java.lang.Short> keys() {
		return set.keySet();
	}

	/**
	 * 返回列比较器集合
	 * @return 返回ColumnComparator集合
	 */
	public java.util.Collection<ColumnComparator> values() {
		return this.set.values();
	}

//	/* (non-Javadoc)
//	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
//	 */
//	@Override
//	public int compare(Row row1, Row row2) {
//		OrderByAdapter next = adapter;
//		int ret = -1;
//		while (next != null) {
//			short columnId = next.getColumnId();
//			Column c1 = row1.find(columnId);
//			Column c2 = row2.find(columnId);
//			
//			if (set.isEmpty()) {
//				ret = c1.compare(c2); // 没有定义比较器时，使用默认的列比较
//			} else {
//				// 找到对应的比较器
//				ColumnComparator comparator = set.get(columnId);
//				if (comparator != null) {
//					ret = comparator.compare(c1, c2); // 用户定义的比较器
//				} else {
//					ret = c1.compare(c2); // 使用默认的列比较
//				}
//			}
//			
//			// 匹配，比较下一个
//			if (ret == 0) {
//				next = next.getNext();
//				continue;
//			}
//
//			// 升序/降序排序
//			if (next.isASC()) {
//				return ret; // 升序排列
//			} else {
//				return (0 - ret); // 相反值，降序排列
//			}
//
//			//			// 升序/降序排序
//			//			if (ret == 0) {
//			//				next = next.getNext();
//			//			} else if (next.isASC()) {
//			//				return ret; // 升序排列
//			//			} else {
//			//				return (0 - ret); // 相反值，降序排列
//			//			}
//
//			// // 如果当前列比较一致，取下一列比较
//			// if (ret == 0) {
//			// next = next.getNext();
//			// } else if (next.isASC()) {
//			// return ret; // 升序排列
//			// } else {
//			// return (ret > 0 ? -1 : 1); // 降序排列
//			// }
//		}
//		return 0;
//	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Row row1, Row row2) {
		OrderByAdapter next = adapter;
		// 循环比较
		while (next != null) {
			short columnId = next.getColumnId();
			Column c1 = row1.find(columnId);
			Column c2 = row2.find(columnId);

			int ret = -1;
			if (set.isEmpty()) {
				ret = c1.compare(c2, next.isASC()); // 没有定义比较器时，使用默认的列比较
			} else {
				// 找到对应的比较器
				ColumnComparator comparator = set.get(columnId);
				if (comparator != null) {
					ret = comparator.compare(c1, c2, next.isASC()); // 用户定义的比较器
				} else {
					ret = c1.compare(c2, next.isASC()); // 使用默认的列比较
				}
			}
			
			// 如果匹配，比较下一个
			if (ret == 0) {
				next = next.getNext();
				// 没有，返回结果
				if (next == null) {
					return ret;
				}
			}
			// 非0值，返回结果
			else {
				return ret;
			}
		}
		
		return 0;
	}
	
}