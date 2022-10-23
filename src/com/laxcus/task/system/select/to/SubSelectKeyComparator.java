/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.util.*;

/**
 * 嵌套检索的列数值比较器。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2014
 * @since laxcus 1.0
 */
public class SubSelectKeyComparator implements Comparator<Column> {

	/**
	 * 
	 */
	public SubSelectKeyComparator() {
		super();
	}

	/**
	 * 比较两列参数的排列顺序。如果类型不一致，将弹出异常。
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Column s1, Column s2) {
		// 如果对象为空指针...
		if (s1 == null) {
			return -1;
		} else if (s2 == null) {
			return 1;
		}
		// 比较空值
		if (s1.isNull()) {
			return -1;
		} else if (s2.isNull()) {
			return 1;
		}

		// 必须类型一致
		if (s1.getType() != s2.getType()) {
			throw new java.lang.ClassCastException();
		}

		// 如果是可变长类型，比较它们的数值字节，其它类型使用默认的比较
		if (s1.isVariable()) {
			byte[] b1 = ((Variable) s1).getValue();
			byte[] b2 = ((Variable) s2).getValue();
			return Laxkit.compareTo(b1, b2);
		} else {
			return s1.compare(s2);
		}
	}

}