/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.chart;

import java.util.*;

import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.index.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 表索引集合 <br><br>
 * 
 * 表索引集合是一个DATA站点、一个数据表空间下面，全部列索引的集合。<br>
 * 它为“SQL WHERE”检索提供定位关联数据块的帮助。
 * 当“SELECT、DELETE”操作发生时，首先通过表索引集合确定关联的数据块编号，再根据数据块编号，进入JNI执行相关的操作。<br><br>
 * 
 * 表索引集合是动态的，随数据更新同步发生变化。<br><br>
 * 
 * 说明：<br>
 * 在LAXCUS的类命名中，“TABLE”是一个静态的概念，相对于，“SHEET”是动态的概念。
 * 
 * @author scott.liang 
 * @version 1.2 11/3/2012
 * @since laxcus 1.0
 */
public class StubChartSheet {

	/** 表名 **/
	private Space space;
	
	/** 封闭状态数据块目录下的剩余磁盘空间尺寸 **/
	private long left;

	/** 数据块文件总长度 **/
	private long available;
	
	/** 数据块数目 **/
	private int stubs;

	/** 列编号 -> 列索引集图表 **/
	private Map<java.lang.Short, StubChart> charts = new TreeMap<java.lang.Short, StubChart>();

	/**
	 * 构造默认的表索引集合
	 */
	public StubChartSheet() {
		super();
		available = 0;
		stubs = 0;
	}

	/**
	 * 构造默认的表索引集合，指定数据表名
	 * @param space  数据表名
	 * @param left  剩余磁盘目录
	 */
	public StubChartSheet(Space space, long left) {
		this();
		setSpace(space);
		setLeft(left);
	}

	/**
	 * 设置数据表名
	 * @param e
	 */
	public void setSpace(Space e) {
		this.space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space
	 */
	public Space getSpace() {
		return this.space;
	}

	/**
	 * 设置剩余空间尺寸
	 * @param i
	 */
	public void setLeft(long i) {
		left = i;
	}

	/**
	 * 返回剩余空间尺寸
	 * @return
	 */
	public long getLeft() {
		return left;
	}
	
	/**
	 * 设置数据块文件总长度
	 * @param len
	 */
	public void setAvailable(long len) {
		available = len;
	}

	/**
	 * 增加数据块文件长度
	 * @param len
	 */
	public void addAvailable(long len) {
		available += len;
	}

	/**
	 * 返回数据块文件总长度
	 * @return
	 */
	public long getAvailable() {
		return available;
	}
	
	/**
	 * 设置数据块数目
	 * @param i 数据块数目
	 */
	public void setStubs(int i) {
		stubs = i;
	}

	/**
	 * 返回数据块数目
	 * @return 数据块数目
	 */
	public int getStubs() {
		return stubs;
	}
	
	/**
	 * 返回内存索引数据总长度。内存索引随时变动，需要实时计算。
	 * @return  返回实时统计值
	 */
	public long getMemoryCapacity() {
		// KEY（SHORT类型）占用两个字节
		long size = (charts.size() * 2);
		// 统计各类型索引的内存容量
		for (StubChart chart : charts.values()) {
			size += chart.capacity();
		}
		// 返回统计值
		return size;
	}

	/**
	 * 根据检索条件，查找匹配的数据块标识号，并写入内存集合
	 * @param condi  检索条件
	 * @param output  数据块编号输出集合
	 * @return
	 */
	public int find(Where condi, StubSet output) {
		int count = 0;
		for(int index = 0; condi != null; index++) {
			StubSet set = new StubSet();
			int size = choice(condi, set);

			switch (condi.getOuterRelation()) {
			case LogicOperator.AND:
				if (size < 0) {
					return -1;
				}
				output.AND(set);
				count += size;
				break;
			case LogicOperator.OR:
				if (size > 0) {
					output.OR(set);
					count += size;
				}
				break;
			default:
				if (index == 0) {
					output.addAll(set);
					count = size;
				} else {
					throw new IllegalArgumentException("invalid condition relate");
				}
			}
			// 下一层检索条件
			condi = condi.next();
		}
		return count;
	}

	/**
	 * 根据检索条件，查找匹配的数据块，返回它的集合
	 * @param condi  检索条件
	 * @return  StubSet
	 */
	public StubSet find(Where condi) {
		StubSet set = new StubSet();
		this.find(condi, set);
		return set;
	}

	/**
	 * 根据检索条件，选择匹配的数据块标识号，并且保存输出
	 * @param condi
	 * @param output
	 * @return int
	 */
	private int choice(Where condi, StubSet output) {
		int count = 0;
		while (condi != null) {
			WhereIndex whereIndex = condi.getIndex();
			short columnId = whereIndex.getColumnId();
			StubChart chart = charts.get(columnId);
			if (chart == null || chart.isEmpty()) {
				Logger.error(this, "choice", "cannot find index by %d", columnId);
				return 0;
			}
			Set<Long> set = chart.find(condi);
			if (!set.isEmpty()) {
				if (condi.isAND()) {
					// 逻辑"与"操作，保存相同的数据块标识号
					output.AND(set);
				} else if (condi.isOR()) {
					// 逻辑"或"操作，累加
					output.OR(set);
				} else {
					output.addAll(set);
				}
				count = set.size();
			}
			// 检查同级关联对象
			for (Where partner : condi.getPartners()) {
				int size = this.choice(partner, output);
				if (size < 1) return -1;
				count += size;
			}
			condi = condi.next();
		}

		Logger.debug(this, "choice", "result is %d", count);

		return count;
	}

	/**
	 * 保存一个数据块索引
	 * @param stub  数据块编号
	 * @param index  数据块索引实例
	 * @return  成功返回“真”，否则“假”。
	 */
	public boolean add(long stub, StubIndex index) {
		Laxkit.nullabled(index);
		
		short columnId = index.getColumnId();
		StubChart element = charts.get(columnId);
		// 如果未定义，生成它
		if (element == null) {
			element = index.createStubChart();
			// 保存索引映像
			charts.put(columnId, element);
		}
		// 保存数据块编号和索引
		return element.add(stub, index.getRange());
	}

	/**
	 * 根据列编号，查找对应的分布索引映像
	 * @param columnId
	 * @return
	 */
	public StubChart find(short columnId) {
		return charts.get(columnId);
	}

	/**
	 * 输出全部数据块编号
	 * @return  StubSet实例
	 */
	public StubSet getStubSet() {
		StubSet set = new StubSet();
		for (StubChart chart : charts.values()) {
			StubSet that = chart.getStubSet();
			set.addAll(that);
		}
		return set;
	}

	/**
	 * 分布索引映像数量
	 * @return
	 */
	public int size() {
		return charts.size();
	}

	/**
	 * 判断分布索引映像集合是否空
	 * @return boolean
	 */
	public boolean isEmpty() {
		return this.size() == 0;
	}

}