/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.section;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 长整型(BIT64)的索引分区集合<br>
 * 
 * @author scott.liang
 * @version 1.2 08/02/2012
 * @version laxcus 1.0
 */
public abstract class Bit64Sector extends ColumnSector {

	private static final long serialVersionUID = 8608770438100976071L;

	/** 长整型范围集合 **/
	protected TreeSet<LongRange> ranges = new TreeSet<LongRange>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		
		writer.writeInt(ranges.size());
		for(LongRange e : ranges) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			LongRange e = new LongRange(reader);
			ranges.add(e);
		}
	}

	/**
	 * 构造一个默认的64位列分区
	 */
	protected Bit64Sector() {
		super();
	}
	
	/**
	 * 根据传入参数，生成一个64位列分区
	 * @param that
	 */
	protected Bit64Sector(Bit64Sector that) {
		super(that);
		ranges.addAll(that.ranges);
	}
	
	/**
	 * 保存一个长整型范围
	 * @param e 长整型范围
	 * @return 保存成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean add(LongRange e){
		Laxkit.nullabled(e);
		
		return ranges.add(e);
	}

	/**
	 * 删除一个长整型范围
	 * @param e 长整型范围
	 * @return 删除成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean remove(LongRange e) {
		Laxkit.nullabled(e);
		
		return ranges.remove(e);
	}

	/**
	 * 输出全部长整型范围
	 * @return LongRange列表
	 */
	public List<LongRange> list() {
		return new ArrayList<LongRange>(ranges);
	}
	
	/**
	 * 清除全部记录
	 */
	public void clear() {
		ranges.clear();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#size()
	 */
	@Override
	public int size() {
		return ranges.size();
	}
	
	/**
	 * 根据传入的长整型值，定位它在分区数组集合中的下标位置
	 * @param value 长整型值
	 * @return 返回传入值所在分区下标位置
	 */
	protected int seekIndex(long value) {
		// 无范围定义是错误
		if (ranges.isEmpty()) {
			throw new ArrayIndexOutOfBoundsException();
		}

		// 找到对应的范围下标位置
		int index = 0;
		for (LongRange e : ranges) {
			if (value < e.begin() || e.inside(value)) {
				return index;
			}
			index++;
		}
		// 如果没有找到分区,默认为最后
		return index - 1;
	}

	/**
	 * 定位一个长整型对象在分区数组中的下标位置
	 * @param that 长整型对象
	 * @return 返回传入值所在分区下标位置
	 */
	protected int seekLong(Object that) {
		long[] array = null;
		if (that.getClass() == java.lang.Long.class) {
			array = new long[] { ((java.lang.Long) that).intValue() };
		} else if (that.getClass() == new long[0].getClass()) {
			array = (long[]) that;
		} else {
			throw new ClassCastException("only long or long array");
		}
		// 空数组
		if (array == null || array.length == 0) {
			return 0;
		}

		return seekIndex(array[0]);
	}

	/**
	 * 将字符串翻译为长整型值
	 * @param input 长整型字符串
	 * @return 返回long类型
	 */
	protected long toLong(String input) {
		long value = 0L;
		int shift = 0;
		int end = input.length();
		while (end > 0) {
			int gap = (end - 2 >= 0 ? 2 : 1);
			String sub = input.substring(end - gap, end);
			long num = java.lang.Long.valueOf(sub, 16) & 0xFF;
			value |= (num << shift);
			shift += 8;
			end -= gap;
		}
		return value;
	}

}