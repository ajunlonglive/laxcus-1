/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.to.evaluate;

import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.util.*;

/**
 * 迭代编号比较器。按照从小到大的顺序排列。
 * 
 * @author scott.liang
 * @version 1.0 5/1/2012
 * @since laxcus 1.0
 */
public class IterateIndexComparator implements Comparator<FluxField> {

	/**
	 * 构造默认的迭代编号比较器
	 */
	public IterateIndexComparator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(FluxField e1, FluxField e2) {
		return Laxkit.compareTo(e1.getIterateIndex(), e2.getIterateIndex());
	}
}
