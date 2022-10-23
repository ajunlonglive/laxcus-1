/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.to;

import java.util.*;

/**
 * 数值排序
 * 
 * @author scott.liang
 * @version 1.0 8/12/2011
 * @since laxcus 1.0
 */
final class IntegerComparator implements Comparator<Integer> {

	/** 降序标记符 **/
	private boolean desc;

	/**
	 * 构造一个整数值比较排序器
	 * @param desc 降序排序
	 */
	public IntegerComparator(boolean desc) {
		super();
		this.desc = desc;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Integer value1, Integer value2) {
		// 降序排序
		if (desc) {
			return (value1 < value2 ? 1 : (value1 > value2 ? -1 : 0));
		} else {
			// 升序排列
			return (value1 < value2 ? -1 : (value1 > value2 ? 1 : 0));
		}
	}

}