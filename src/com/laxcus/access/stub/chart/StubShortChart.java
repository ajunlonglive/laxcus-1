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
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.util.*;
import com.laxcus.util.range.*;
import com.laxcus.util.set.*;

/**
 * 短整型索引范围图表。
 * 
 * @author scott.liang
 * @version 1.1 6/27/2012
 * @since laxcus 1.0
 */
public class StubShortChart implements StubChart {

	/** SHORT索引范围  -> 索引范围的数据块和DATA节点地址集合 **/
	private Map<ShortRange, StubSet> charts = new TreeMap<ShortRange, StubSet>();

	/**
	 * 构造默认的短整型索引范围图表
	 */
	public StubShortChart() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.chart.StubChart#add(long, com.laxcus.util.range.Range)
	 */
	@Override
	public boolean add(long stub, Range range) {
		// 范围类型必须一致
		if (range.getClass() != ShortRange.class) {
			throw new ClassCastException();
		}
		ShortRange sub = (ShortRange) range;
		StubSet set = charts.get(sub);
		if (set == null) {
			set = new StubSet();
			charts.put(sub, set);
		}
		return set.add(stub);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.chart.StubChart#find(com.laxcus.access.command.Condition)
	 */
	@Override
	public Set<Long> find(Where condi) {
		WhereIndex index = condi.getIndex();
		if (index == null) {
			throw new NullPointerException();
		} else if (index.getClass() != ShortIndex.class) {
			throw new IllegalValueException("illega class %s", index.getClass().getName());
		}
		short hash = ((ShortIndex) index).getHash();

		// 找到范围分片截止点
		TreeSet<Long> records = new TreeSet<Long>();

		Iterator<Map.Entry<ShortRange, StubSet>> iterator = charts.entrySet().iterator();

		// 列名在左，参数值在右(格式固定，检查可能的范围)
		switch (condi.getCompare()) {
		case CompareOperator.IS_NULL:
		case CompareOperator.EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<ShortRange, StubSet> entry = iterator.next();
				if (entry.getKey().inside(hash)) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.NOT_NULL:
		case CompareOperator.NOT_EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<ShortRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() != hash || entry.getKey().end() != hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.LESS:
			while (iterator.hasNext()) {
				Map.Entry<ShortRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() < hash || entry.getKey().end() < hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.LESS_EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<ShortRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() <= hash || entry.getKey().end() <= hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.GREATER:
			while (iterator.hasNext()) {
				Map.Entry<ShortRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() > hash || entry.getKey().end() > hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.GREATER_EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<ShortRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() >= hash || entry.getKey().end() >= hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.LIKE:
			while (iterator.hasNext()) {
				Map.Entry<ShortRange, StubSet> entry = iterator.next();
				records.addAll(entry.getValue().list());
			}
			break;			
		}
		return records;
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.access.chart.StubChart#choice(boolean, java.lang.Number[], java.lang.Number[])
	//	 */
	//	@Override
	//	public IndexZone[] choice(boolean ignore, Number[] b, Number[] e) {
	//		ShortRange[] ranges = null;
	//		if (ignore) {
	//			if (b == null || e == null) {
	//				throw new NullPointerException("number array null pointer");
	//			} else if (b.length != e.length) {
	//				throw new IllegalArgumentException("number size not match!");
	//			}
	//			ranges = new ShortRange[b.length];
	//			for (int i = 0; i < b.length; i++) {
	//				if (b[i].getClass() != java.lang.Short.class || e[i].getClass() != java.lang.Short.class) {
	//					throw new ClassCastException("this not java.lang.Short class!");
	//				}
	//				ranges[i] = new ShortRange(((java.lang.Short) b[i]).shortValue(), ((java.lang.Short) e[i]).shortValue());
	//			}
	//		}
	//
	//		List<ShortZone> array = new ArrayList<ShortZone>(charts.size());
	//		Iterator<Map.Entry<ShortRange, StubSet>> iterators = charts.entrySet().iterator();
	//		while (iterators.hasNext()) {
	//			Map.Entry<ShortRange, StubSet> entry = iterators.next();
	//			boolean skip = false;
	//			for (int i = 0; ranges != null && i < ranges.length; i++) {
	//				skip = (ranges[i].equals(entry.getKey()));
	//				if (skip) break;
	//			}
	//			if (skip) continue;
	//			ShortZone si = new ShortZone(entry.getKey(), entry.getValue().size());
	//			array.add((ShortZone) si.clone());
	//		}
	//
	//		if(array.isEmpty()) return null;
	//		ShortZone[] a = new ShortZone[array.size()];
	//		return array.toArray(a);
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.access.chart.StubChart#choice(boolean)
	//	 */
	//	@Override
	//	public IndexZone[] choice(boolean ignore) {
	//		if (ignore) {
	//			return choice(true, new java.lang.Short[] { java.lang.Short.MIN_VALUE },
	//					new java.lang.Short[] { java.lang.Short.MAX_VALUE });
	//		} else {
	//			return this.choice(false, null, null);
	//		}
	//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.chart.StubChart#getStubSet()
	 */
	@Override
	public StubSet getStubSet(){
		StubSet array = new StubSet();
		for (StubSet set : charts.values()) {
			array.addAll(set);
		}
		return array;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.chart.StubChart#capacity()
	 */
	@Override
	public long capacity() {
		// 每个SHORT范围占用4个字节
		long size = (charts.size() * 4);
		// 每个数据块占用8个字节
		for (StubSet set : charts.values()) {
			size += (set.size() * 8);
		}
		return size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.chart.StubChart#size()
	 */
	@Override
	public int size() {
		return charts.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.chart.StubChart#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

}
