/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.range;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 整数范围。
 * 
 * @author scott.liang
 * @version 1.0 1/19/2009
 * @since laxcus 1.0
 */
public final class IntegerRange extends Range implements Comparable<IntegerRange> {

	private static final long serialVersionUID = -5512712742611116141L;

	/** 数值分布范围(begin必须小于或者等于end) */
	private int begin, end;

	/**
	 * 将整型值范围写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInt(begin);
		writer.writeInt(end);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析整型范围
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		int s1 = reader.readInt();
		int s2 = reader.readInt();
		set(s1, s2);
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的整型值范围，生成它的副本
	 * @param that
	 */
	private IntegerRange(IntegerRange that) {
		super();
		this.begin = that.begin;
		this.end = that.end;
	}

	/**
	 * 构造一个默认的整型值范围
	 */
	public IntegerRange() {
		super();
		begin = end = 0;
	}

	/**
	 * 构造一个整型值范围，并且指定它的范围值
	 * @param begin
	 * @param end
	 */
	public IntegerRange(int begin, int end) {
		this();
		this.set(begin, end);
	}

	/**
	 * 从可类化读取器中解析整型范围
	 * @param reader
	 */
	public IntegerRange(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 计算交叠区域，如果不成立返回null
	 * @param left 左侧范围，开始位置(begin)必须小于等于右侧
	 * @param right 右侧范围
	 * @return
	 */
	private final IntegerRange intersection(IntegerRange left, IntegerRange right) {
		int begin = right.begin;
		// 选择最小的结束位置
		int end = (left.end <= right.end ? left.end : right.end);
		return (begin <= end ? new IntegerRange(begin, end) : null);
	}

	/**
	 * "交"计算。返回交叠区域，如果不相交，返回null。
	 * @param that
	 * @return
	 */
	public IntegerRange intersection(IntegerRange that) {
		if (this.begin <= that.begin) {
			return intersection(this, that);
		} else {
			return intersection(that, this);
		}
	}

	/**
	 * "差"计算。返回当前对象与传入对象不相交的部分。计算结果有3：<br>
	 * 1. 如果完全重叠，或者当前对象在传入对象范围内，返回null。
	 * 2. 如果完全不重叠，返回当前对象。
	 * 3. 如果有部分重叠，返回不重叠的部分。不重叠的部分可能是1或者2个数组。
	 * @param that
	 * @return
	 */
	public IntegerRange[] difference(IntegerRange that) {
		IntegerRange inter = this.intersection(that);
		// 完全不重叠，返回当前对象
		if (inter == null) {
			return new IntegerRange[] { new IntegerRange(this) };
		}
		// 比较检查，截取两侧不相交的部分
		ArrayList<IntegerRange> array = new ArrayList<IntegerRange>();
		if (this.begin < inter.begin) {
			array.add(new IntegerRange(this.begin, inter.begin - 1));
		}
		if (inter.end < this.end) {
			array.add(new IntegerRange(inter.end + 1, this.end));
		}
		if (array.size() > 0) {
			IntegerRange[] s = new IntegerRange[array.size()];
			return array.toArray(s);
		}
		return null;
	}

	/**
	 * 设置数据范围，开始位置必须小于等于结束位置。否则抛出"算术异常"
	 * @param b 开始位置
	 * @param e 结束位置
	 * @throws ArithmeticException
	 */
	public void set(int b, int e) {
		if (b > e) {
			String s = String.format("range out! %d > %d", b, e);
			throw new ArithmeticException(s);
		}
		begin = b;
		end = e;
	}

	/**
	 * 设置数据范围
	 * @param e IntegerRange实例
	 */
	public void set(IntegerRange e) {
		set(e.begin, e.end);
	}

	/**
	 * 返回开始点
	 * @return
	 */
	public int begin() {
		return this.begin;
	}

	/**
	 * 返回结束点
	 * @return
	 */
	public int end() {
		return this.end;
	}

	/**
	 * 统计它的范围尺寸
	 * @return
	 */
	public int size() {
		return end - begin + 1;
	}

	/**
	 * 检查一个值是在范围内
	 * @param value
	 * @return
	 */
	public boolean inside(int value) {
		return (begin <= value && value <= end);
	}

	/**
	 * 检查一个范围是否包含在当前范围内
	 * @param that
	 * @return
	 */
	public boolean inside(IntegerRange that) {
		return (begin <= that.begin && that.end <= end);
	}

	/**
	 * 根据传入的块数，将当前范围平均分割多指定的多个范围
	 * @param blocks 分割块数
	 * @return
	 */
	public IntegerRange[] split(int blocks) {
		// 块数小于1即错误
		if (blocks < 1) {
			throw new IllegalArgumentException("illegal blocks:" + blocks);
		}

		BigIntegerRange range = new BigIntegerRange(
				Integer.toString(this.begin), Integer.toString(this.end), 10);

		// 分割块
		BigIntegerRange[] res = range.split(blocks);
		// 重新生成结果
		IntegerRange[] ranges = new IntegerRange[res.length];
		for (int i = 0; i < res.length; i++) {
			ranges[i] = new IntegerRange(res[i].begin().intValue(),
					res[i].end().intValue());
		}
		return ranges;
	}

	/*
	 * 比较范围是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != IntegerRange.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return this.compareTo((IntegerRange) that) == 0;
	}

	/*
	 * 哈希码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return begin ^ end;
	}

	/*
	 * 字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d - %d", begin, end);
	}

	/**
	 * 根据传入的范围参数，比较两个对象的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IntegerRange that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(begin, that.begin);
		if(ret == 0) {
			ret = Laxkit.compareTo(end, that.end);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.range.NumberRange#duplicate()
	 */
	@Override
	public IntegerRange duplicate() {
		return new IntegerRange(this);
	}

}