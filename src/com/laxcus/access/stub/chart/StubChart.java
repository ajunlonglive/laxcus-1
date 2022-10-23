/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.chart;

import java.util.*;

import com.laxcus.command.access.*;
import com.laxcus.util.range.*;
import com.laxcus.util.set.*;

/**
 * 列索引集合 <br><br>
 * 
 * 列索引集合是是一个DATA站点、一个数据表空间下面，基一列索引的集合。它为基于列的“SQL WHERE”检索提供帮助。<br>
 * 在它的上面是StubChartSheet，汇集了一个表空间的全部列索引。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 6/27/2012
 * @since laxcus 1.0
 * 
 * @see Where
 */
public interface StubChart {

	/**
	 * 记录一个数据块的编号和索引范围
	 * 
	 * @param stub  数据块编号
	 * @param range  索引范围
	 * @return  成功返回“真”，否则“假”。
	 */
	boolean add(long stub, Range range);

	/**
	 * 根据检索条件，查找关联的数据块编号 
	 * @param condition  检索条件
	 * @return  返回匹配的数据块编号集合
	 */
	Set<Long> find(Where condition);

	/**
	 * 输出全部数据块编号集合
	 * @return  StubSet实例
	 */
	StubSet getStubSet();

	/**
	 * 返回索引数据内存容量
	 * @return 内存容量的长整型值
	 */
	long capacity();

	/**
	 * 返回列索引集合中的索引范围尺寸
	 * @return 索引范围的整型值
	 */
	int size();

	/**
	 * 判断列索引集合是空
	 * @return 返回真或者假
	 */
	boolean isEmpty();
}