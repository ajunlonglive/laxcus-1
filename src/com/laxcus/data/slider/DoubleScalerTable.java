/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.slider;

import java.util.*;

import com.laxcus.access.index.zone.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 双浮点列码位统计表。
 * 
 * @author scott.liang
 * @version 1.1 5/16/2015
 * @since laxcus 1.0
 */
public class DoubleScalerTable extends ScalerTable {

	private static final long serialVersionUID = -2288875432749244773L;

	/** 码位范围 -> 统计值 */
	private Map<DoubleRange, java.lang.Integer> ranges = new TreeMap<DoubleRange, java.lang.Integer>();

	/**
	 * 构造默认的双浮点列码位统计表
	 */
	public DoubleScalerTable() {
		super(ScaleType.DOUBLE_SCALE);
	}

	/**
	 * 根据传入的双浮点列码位统计表，生成它的浅层数据副本
	 * @param that DoubleScaleTable实例
	 */
	private DoubleScalerTable(DoubleScalerTable that) {
		super(that);
		ranges.putAll(that.ranges);
	}

	/**
	 * 构造双浮点列码位统计表，指定列空间
	 * @param dock 列空间实例
	 */
	public DoubleScalerTable(Dock dock) {
		this();
		setDock(dock);
	}

	/**
	 * 从可类化数据读取器中解析双浮点列码位统计表
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DoubleScalerTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#duplicate()
	 */
	@Override
	public DoubleScalerTable duplicate() {
		return new DoubleScalerTable(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.scale.table.ScaleTable#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// 写入成员数目
		writer.writeInt(ranges.size());
		// 写入每一个成员参数
		Iterator<Map.Entry<DoubleRange, java.lang.Integer>> iterator = ranges.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<DoubleRange, java.lang.Integer> entry = iterator.next();
			writer.writeObject(entry.getKey());	// 代码位范围
			writer.writeInt(entry.getValue());	// 统计值
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.scale.table.ScaleTable#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		// 成员数目
		int size = reader.readInt();
		// 解析成员参数
		for(int i = 0; i < size; i++){
			DoubleRange key = new DoubleRange(reader);
			int value = reader.readInt();
			ranges.put(key, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#put()
	 */
	@Override
	public IndexZone[] put() {
		ArrayList<DoubleZone> array = new ArrayList<DoubleZone>();
		Iterator<Map.Entry<DoubleRange, Integer>> iterator = ranges.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<DoubleRange, Integer> entry = iterator.next();
			DoubleZone zone = new DoubleZone(entry.getKey(), entry.getValue());
			array.add(zone);
		}

		DoubleZone[] a = new DoubleZone[array.size()];
		return array.toArray(a);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#add(java.lang.Number, int)
	 */
	@Override
	public void add(Number codePoint, int count) {
		if (codePoint.getClass() != java.lang.Double.class) {
			throw new ClassCastException();
		}
		if (count < 1) {
			throw new IllegalValueException("illegal count %d", count);
		}
		double scale = ((java.lang.Double) codePoint).doubleValue();

		for (DoubleRange range : ranges.keySet()) {
			if (range.inside(scale)) {
				java.lang.Integer value = ranges.get(range);
				value += count;
				return;
			} else if (scale + 1 == range.begin()) {
				java.lang.Integer value = ranges.get(range);
				DoubleRange it = new DoubleRange(scale, range.end());
				ranges.remove(range);
				ranges.put(it, value + count);
				return;
			} else if (range.end() + 1 == scale) {
				java.lang.Integer value = ranges.get(range);
				DoubleRange it = new DoubleRange(range.begin(), scale);
				ranges.remove(range);
				ranges.put(it, value + count);
				return;
			}
		}

		// 写一个新的区域
		ranges.put(new DoubleRange(scale, scale), count);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#keys()
	 */
	@Override
	public List<Range> keys() {
		return new ArrayList<Range>(ranges.keySet());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#find(com.laxcus.util.range.NumberRange)
	 */
	@Override
	public java.lang.Integer find(Range key) {
		return ranges.get(key);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#size()
	 */
	@Override
	public int size() {
		return ranges.size();
	}

	//	public static void main(String[] args) {
	//		DoubleScaleTable cabin = new DoubleScaleTable(new Dock("media", "music", (short)2));
	//		for(char w ='A'; w<='Z'; w++) {
	//			cabin.add( new java.lang.Integer(w) , 1);
	//		}
	//		cabin.add(new java.lang.Integer('哥'), 1);
	//		cabin.add(new java.lang.Integer('全'), 1);
	//		cabin.add(new java.lang.Integer('哈'), 2);
	//		System.out.printf("size is:%d\n", cabin.size());
	//
	//		IndexZone[] zones = cabin.put();
	//		for (int i = 0; i < zones.length; i++) {
	//					System.out.printf("%s,%d\n", ((DoubleZone)zones[i]).getRange(),
	//							zones[i].getWeight());
	//			System.out.printf("%d\n", zones[i].getWeight());
	//		}
	//
	//		byte[] b = cabin.build();
	//		System.out.printf("build size:%d\n", b.length);
	//		DoubleScaleTable cabin2 = new DoubleScaleTable();
	//		int len = cabin2.resolve(b, 0, b.length);
	//		System.out.printf("resolve size:%d\n", len);
	//	}

}