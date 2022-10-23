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
 * 整型索引范围图表
 * 
 * @author scott.liang
 * @version 1.1 6/27/2012
 * @since laxcus 1.0
 */
public class StubIntegerChart implements StubChart {

	/** 整型值索引分布范围 -> 数据块集合  */
	private Map<IntegerRange, StubSet> charts = new TreeMap<IntegerRange, StubSet>();

	/**
	 * 构造默认的整型索引范围图表
	 */
	public StubIntegerChart() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.chart.StubChart#add(long, com.laxcus.util.range.Range)
	 */
	@Override
	public boolean add(long stub, Range range) {
		// 范围类型必须一致
		if (range.getClass() != IntegerRange.class) {
			throw new ClassCastException();
		}
		IntegerRange sub = (IntegerRange) range;
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
		} else if (index.getClass() != IntegerIndex.class) {
			throw new IllegalValueException("illega class %s", index.getClass().getName());
		}
		
		int hash = ((IntegerIndex) index).getHash();

		// 找到范围分片截止点
		TreeSet<Long> records = new TreeSet<Long>();

		Iterator<Map.Entry<IntegerRange, StubSet>> iterator = charts.entrySet().iterator();

		// 列名在左，参数值在右(格式固定，检查可能的范围)
		switch (condi.getCompare()) {
		case CompareOperator.IS_NULL:
		case CompareOperator.EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<IntegerRange, StubSet> entry = iterator.next();
				if (entry.getKey().inside(hash)) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.NOT_NULL:
		case CompareOperator.NOT_EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<IntegerRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() != hash || entry.getKey().end() != hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.LESS:
			while (iterator.hasNext()) {
				Map.Entry<IntegerRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() < hash || entry.getKey().end() < hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.LESS_EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<IntegerRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() <= hash || entry.getKey().end() <= hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.GREATER:
			while (iterator.hasNext()) {
				Map.Entry<IntegerRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() > hash || entry.getKey().end() > hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.GREATER_EQUAL:
			while (iterator.hasNext()) {
				Map.Entry<IntegerRange, StubSet> entry = iterator.next();
				if (entry.getKey().begin() >= hash || entry.getKey().end() >= hash) {
					records.addAll(entry.getValue().list());
				}
			}
			break;
		case CompareOperator.LIKE:
			while (iterator.hasNext()) {
				Map.Entry<IntegerRange, StubSet> entry = iterator.next();
				records.addAll(entry.getValue().list());
			}
			break;			
		}
		return records;
	}


	//	/**
	//	 * 筛选索引区(过滤不符合条件的，保留符合条件的)
	//	 * @param ignore - 是否过滤
	//	 * @param b - 开始位置下标
	//	 * @param e - 结束位置下标
	//	 * @return
	//	 */
	//	@Override
	//	public IndexZone[] choice(boolean ignore, Number[] b, Number[] e) {
	//		IntegerRange[] ranges = null;
	//		if (ignore) {
	//			if (b == null || e == null) {
	//				throw new NullPointerException("number array is null pointer");
	//			} else if (b.length != e.length) {
	//				throw new IllegalArgumentException("number size not match!");
	//			}
	//			ranges = new IntegerRange[b.length];
	//			for (int i = 0; i < b.length; i++) {
	//				if (b[i].getClass() != java.lang.Integer.class || e[i].getClass() != java.lang.Integer.class) {
	//					throw new ClassCastException("this not java.lang.Integer class!");
	//				}
	//				ranges[i] = new IntegerRange(((java.lang.Integer) b[i]).intValue(), ((java.lang.Integer) e[i]).intValue());
	//			}
	//		}
	//
	//		List<IntegerZone> array = new ArrayList<IntegerZone>(charts.size());
	//		Iterator<Map.Entry<IntegerRange, StubSet>> iterators = charts.entrySet().iterator();
	//		while (iterators.hasNext()) {
	//			Map.Entry<IntegerRange, StubSet> entry = iterators.next();
	//			boolean skip = false;
	//			for (int i = 0; ranges != null && i < ranges.length; i++) {
	//				skip = (ranges[i].equals(entry.getKey()));
	//				if (skip) break;
	//			}
	//			if (skip) continue;
	//			IntegerZone si = new IntegerZone(entry.getKey(), entry.getValue().size());
	//			array.add((IntegerZone) si.clone());
	//		}
	//
	//		if(array.isEmpty()) return null;
	//
	//		IntegerZone[] s = new IntegerZone[array.size()];
	//		return array.toArray(s);
	//	}
	//
	//	/**
	//	 * 筛选索引区
	//	 * @param ignore - is true, 过滤标准索引区(是小和最大值)
	//	 * @return
	//	 */
	//	@Override
	//	public IndexZone[] choice(boolean ignore) {
	//		if (ignore) {
	//			return choice(true, new java.lang.Integer[] { java.lang.Integer.MIN_VALUE },
	//					new java.lang.Integer[] { java.lang.Integer.MAX_VALUE });
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
		// 每个INT范围占用8个字节
		long size = (charts.size() * 8);
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
