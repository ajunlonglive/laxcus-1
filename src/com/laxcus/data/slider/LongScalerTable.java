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
 * 长整形列码位统计表。
 * 
 * @author scott.liang
 * @version 1.1 5/16/2015
 * @since laxcus 1.0
 */
public class LongScalerTable extends ScalerTable {

	private static final long serialVersionUID = -2288875432749244773L;

	/** 码位范围 -> 统计值 */
	private Map<LongRange, java.lang.Integer> ranges = new TreeMap<LongRange, java.lang.Integer>();

	/**
	 * 构造默认长整形列码位统计表
	 */
	public LongScalerTable() {
		super(ScaleType.LONG_SCALE);
	}

	/**
	 * 根据传入的长整形列码位统计表，生成它的浅层数据副本
	 * @param that
	 */
	private LongScalerTable(LongScalerTable that) {
		super(that);
		ranges.putAll(that.ranges);
	}

	/**
	 * 构造长整形列码位统计表，指定列空间
	 * @param dock 列空间实例
	 */
	public LongScalerTable(Dock dock) {
		this();
		setDock(dock);
	}

	/**
	 * 从可类化数据读取器中解析长整形列码位统计表
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public LongScalerTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#duplicate()
	 */
	@Override
	public LongScalerTable duplicate() {
		return new LongScalerTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		// 写入成员数目
		writer.writeInt(ranges.size());
		// 写入每一个成员参数
		Iterator<Map.Entry<LongRange, java.lang.Integer>> iterator = ranges.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<LongRange, java.lang.Integer> entry = iterator.next();
			writer.writeObject(entry.getKey()); // 代码位范围
			writer.writeInt(entry.getValue()); // 统计值
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		// 成员数目
		int size = reader.readInt();
		// 解析成员参数
		for(int i = 0; i < size; i++){
			LongRange key = new LongRange(reader);
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
		ArrayList<LongZone> array = new ArrayList<LongZone>();
		Iterator<Map.Entry<LongRange, Integer>> iterator = ranges.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<LongRange, Integer> entry = iterator.next();
			LongZone zone = new LongZone(entry.getKey(), entry.getValue());
			array.add(zone);
		}

		LongZone[] a = new LongZone[array.size()];
		return array.toArray(a);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.codepoint.ScaleTable#add(java.lang.Number, int)
	 */
	@Override
	public void add(Number code, int count) {
		if (code.getClass() != java.lang.Long.class) {
			throw new ClassCastException();
		}
		if (count < 1) {
			throw new IllegalValueException("illegal count %d", count);
		}
		long scale = ((java.lang.Long) code).longValue();

		for (LongRange range : ranges.keySet()) {
			if (range.inside(scale)) {
				java.lang.Integer value = ranges.get(range);
				value += count;
				return;
			} else if (scale + 1 == range.begin()) {
				java.lang.Integer value = ranges.get(range);
				LongRange it = new LongRange(scale, range.end());
				ranges.remove(range);
				ranges.put(it, value + count);
				return;
			} else if (range.end() + 1 == scale) {
				java.lang.Integer value = ranges.get(range);
				LongRange it = new LongRange(range.begin(), scale);
				ranges.remove(range);
				ranges.put(it, value + count);
				return;
			}
		}

		// 写一个新的区域
		ranges.put(new LongRange(scale, scale), count);
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
//		LongScaleTable cabin = new LongScaleTable(new Dock("media", "music", (short)2));
//		for(char w ='A'; w<='Z'; w++) {
//			cabin.add( new java.lang.Long(w) , 1);
//		}
//		cabin.add(new java.lang.Long('哥'), 1);
//		cabin.add(new java.lang.Long('全'), 1);
//		cabin.add(new java.lang.Long('哈'), 2);
//		System.out.printf("size is:%d\n", cabin.size());
//
//		IndexZone[] zones = cabin.put();
//		for (int i = 0; i < zones.length; i++) {
//					System.out.printf("%s,%d\n", ((LongZone)zones[i]).getRange(),
//							zones[i].getWeight());
//			System.out.printf("%d\n", zones[i].getWeight());
//		}
//
//		byte[] b = cabin.build();
//		System.out.printf("build size:%d\n", b.length);
//		LongScaleTable cabin2 = new LongScaleTable();
//		int len = cabin2.resolve(b, 0, b.length);
//		System.out.printf("resolve size:%d\n", len);
//	}
	
}