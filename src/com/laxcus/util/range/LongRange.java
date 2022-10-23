/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.range;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 长整数数据范围。
 * 
 * @author scott.liang
 * @version 1.0 1/19/2009
 * @since laxcus 1.0
 */
public final class LongRange extends Range implements Comparable<LongRange> {

	private static final long serialVersionUID = -3670336457788939907L;

	/** 长整值数据范围  **/
	private long begin, end;

	/**
	 * 将长整型值范围写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeLong(begin);
		writer.writeLong(end);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析长整型范围
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		long s1 = reader.readLong();
		long s2 = reader.readLong();
		set(s1, s2);
		return reader.getSeek() - seek;
	}

	/**
	 * 使用传入长整型范围实例，生成它的数据副本
	 * @param that
	 */
	private LongRange(LongRange that) {
		super(that);
		begin = that.begin;
		end = that.end;
	}

	/**
	 * 生成一个默认的长整型范围
	 */
	public LongRange() {
		super();
		begin = end = 0L;
	}

	/**
	 * 生成一个长整型范围，并且指定它的开始和结束位置
	 * @param begin 开始位置
	 * @param end 结束位置
	 */
	public LongRange(long begin, long end) {
		this();
		set(begin, end);
	}

	/**
	 * 从可类化读取器中解析长整型范围
	 * @param reader
	 */
	public LongRange(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据范围，开始位置必须小于等于结束位置。否则抛出"算术异常"
	 * @param b 开始
	 * @param e 结束
	 * @throws ArithmeticException
	 */
	public void set(long b, long e) {
		if (b > e) {
			String s = String.format("range out! %d > %d", b, e);
			throw new ArithmeticException(s);
		}
		begin = b;
		end = e;
	}

	/**
	 * 返回开始点
	 * @return long
	 */
	public long begin() {
		return begin;
	}

	/**
	 * 返回结束点
	 * @return long
	 */
	public long end() {
		return end;
	}

	/**
	 * 有效空间尺寸
	 * @return
	 */
	public long size() {
		return end - begin + 1;
	}

	/**
	 * 传入的值是否在范围内
	 * @param value
	 * @return
	 */
	public boolean inside(long value) {
		return (begin <= value && value <= end);
	}

	/**
	 * 传入对象是否在范围内
	 * @param that
	 * @return
	 */
	public boolean inside(LongRange that) {
		return (begin <= that.begin && that.end <= end);
	}

	/**
	 * 根据传入的块数，将当前范围平均分割多指定的多个范围
	 * @param blocks
	 * @return
	 */
	public LongRange[] split(int blocks) {
		// 最小分块是1,<1即出错
		if (blocks < 1) {
			throw new IllegalArgumentException("illegal blocks:" + blocks);
		}

		// 分割块
		BigIntegerRange range = new BigIntegerRange(Long.toString(begin),
				Long.toString(end), 10);

		BigIntegerRange[] res = range.split(blocks);
		// 重新输出结果
		LongRange[] ranges = new LongRange[res.length];
		for (int i = 0; i < res.length; i++) {
			ranges[i] = new LongRange(res[i].begin().longValue(), 
					res[i].end().longValue());
		}
		return ranges;
	}

	/**
	 * 比较范围是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != LongRange.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((LongRange) that) == 0;
	}

	/*
	 * 散列码
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (begin ^ end);
	}

	/**
	 * 长整型范围的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d - %d", begin, end);
	}

	/**
	 * 对两个长整型对象排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LongRange that) {
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
	public LongRange duplicate() {
		return new LongRange(this);
	}

}