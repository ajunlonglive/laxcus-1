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
 * BIT32整型的索引扇形区域集合。<br>
 * 
 * @author scott.liang
 * @version 1.2 08/02/2012
 * @since laxcus 1.0
 */
public abstract class Bit32Sector extends ColumnSector {

	private static final long serialVersionUID = -5996996219181641982L;

	/** 范围定义 **/
	protected TreeSet<IntegerRange> ranges = new TreeSet<IntegerRange>();
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		
		writer.writeInt(ranges.size());
		for (IntegerRange e : ranges) {
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
			IntegerRange e = new IntegerRange(reader);
			ranges.add(e);
		}
	}

	/**
	 * 构造一个默认的32位列分区
	 */
	protected Bit32Sector() {
		super();
	}

	/**
	 * 根据传入的Bit32Sector实例，生成它的副本
	 * @param that Bit32Sector实例
	 */
	protected Bit32Sector(Bit32Sector that) {
		super(that);
		ranges.addAll(that.ranges);
	}

	/**
	 * 保存一个整形范围
	 * @param e 整形范围
	 * @return 保存成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean add(IntegerRange e) {
		Laxkit.nullabled(e);

		return ranges.add(e);
	}

	/**
	 * 保存一个整形范围
	 * @param begin 开始
	 * @param end 结束
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(int begin, int end) {
		return add(new IntegerRange(begin, end));
	}

	/**
	 * 删除一个整形范围
	 * @param e 整形范围
	 * @return 删除成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean remove(IntegerRange e) {
		Laxkit.nullabled(e);

		return ranges.remove(e);
	}

	/**
	 * 输出全部整形范围
	 * @return IntegerRange列表
	 */
	public List<IntegerRange> list() {
		return new ArrayList<IntegerRange>(ranges);
	}

	/**
	 * 清除全部
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
	 * 查找某个值所在的范围
	 * @param value 输入值
	 * @return 返回输入值的包含范围，或者空指针
	 */
	public IntegerRange inside(int value) {
		for (IntegerRange range : ranges) {
			if (range.inside(value)) {
				return range;
			}
		}
		return null;
	}

	/**
	 * 根据传入的值， 检查它的分片集合下标位置。下标从0开始
	 * @param value 输入整型值
	 * @return 对象所在集合的下标
	 */
	protected int seekIndex(int value) {
		// 无范围定义是错误
		if (ranges.isEmpty()) {
			throw new ArrayIndexOutOfBoundsException();
		}

		int index = 0;
		for (IntegerRange e : ranges) {
			if (value < e.begin() || e.inside(value)) {
				return index;
			}
			index++;
		}
		return index - 1;
	}

	/**
	 * 定位一个整型值对象在分区数组中的下标位置
	 * @param that 整形对象
	 * @return 对象所在集合的下标
	 */
	protected int seekInteger(Object that) {
		if (that == null) {
			return 0;
		}

		int[] array = null;
		// 判断类型
		if (that.getClass() == java.lang.Integer.class) {
			array = new int[] { ((java.lang.Integer) that).intValue() };
		} else if (that.getClass() == new int[0].getClass()) {
			array = (int[]) that;
		} else {
			throw new ClassCastException("only integer or int array");
		}
		// 空数组
		if (array == null || array.length == 0) {
			return 0;
		}
		return seekIndex(array[0]);
	}

	/**
	 * 将字符串翻译成整型值并且输出
	 * @param input 输入字符串
	 * @return 返回整形值
	 */
	protected int toInteger(String input) {
		int value = 0;
		int shift = 0;
		int end = input.length();
		while (end > 0) {
			int gap = (end - 2 >= 0 ? 2 : 1);
			String sub = input.substring(end - gap, end);
			int num = java.lang.Integer.valueOf(sub, 16) & 0xFF;
			value |= (num << shift);
			shift += 8;
			end -= gap;
		}
		return value;
	}

}