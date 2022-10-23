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
 * 短整型数值范围。<br>
 * 
 * @author scott.liang
 * @version 1.1 1/21/2015
 * @since laxcus 1.0
 */
public final class ShortRange extends Range implements Comparable<ShortRange> {

	private static final long serialVersionUID = 395210867190539800L;

	/** 短整型范围 **/
	private short begin, end;

	/**
	 * 将短整型范围写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeShort(begin);
		writer.writeShort(end);
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析短整型范围
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		short s1 = reader.readShort();
		short s2 = reader.readShort();
		this.set(s1, s2);
		return reader.getSeek() - scale;
	}

	/**
	 * 使用传入参数构造一个副本
	 * @param that
	 */
	private ShortRange(ShortRange that) {
		super(that);
		this.begin = that.begin;
		this.end = that.end;
	}

	/**
	 * 构造一个空的短整型范围
	 */
	public ShortRange() {
		super();
		begin = end = 0;
	}

	/**
	 * 构造一个短整型范围
	 * @param begin
	 * @param end
	 */
	public ShortRange(short begin, short end) {
		this();
		this.set(begin, end);
	}

	/**
	 * 从可类化数据读取器中解析短整型数值范围
	 * @param reader - 可类化数据读取器
	 * @since 1.1
	 */
	public ShortRange(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 设置数据范围，开始位置必须小于等于结束位置。否则抛出"算术异常"
	 * @param b 开始
	 * @param e 结束
	 * @throws ArithmeticException
	 */
	public void set(short b, short e) {
		if (b > e) {
			String s = String.format("range out! %d > %d", b, e);
			throw new ArithmeticException(s);
		}
		this.begin = b;
		this.end = e;
	}

	/**
	 * 返回开始位置
	 * @return
	 */
	public short begin() {
		return this.begin;
	}

	/**
	 * 返回结果位置
	 * @return
	 */
	public short end() {
		return this.end;
	}

	/**
	 * 是否包含
	 * @param value
	 * @return
	 */
	public boolean inside(short value) {
		return begin <= value && value <= end;
	}

	/**
	 * 根据传入的块数，将当前范围平均分割多指定的多个范围
	 * @param blocks
	 * @return
	 */
	public ShortRange[] split(int blocks) {
		// 最小分块是1,<1即出错
		if (blocks < 1) {
			throw new IllegalArgumentException("illegal blocks:" + blocks);
		}

		BigIntegerRange range = new BigIntegerRange(Short.toString(this.begin),
				Short.toString(this.end), 10);

		BigIntegerRange[] res = range.split(blocks);
		ShortRange[] ranges = new ShortRange[res.length];
		for (int i = 0; i < res.length; i++) {
			ranges[i] = new ShortRange(res[i].begin().shortValue(), res[i].end().shortValue());
		}
		return ranges;
	}

	/*
	 * 比较两个范围值是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ShortRange.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return this.compareTo((ShortRange) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return begin ^ end;
	}

	/*
	 * 生成字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d - %d", this.begin, this.end);
	}

	/*
	 * 比较两个对象的排列位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShortRange that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(begin, that.begin);
		if (ret == 0) {
			ret = Laxkit.compareTo(end, that.end);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.range.NumberRange#duplicate()
	 */
	@Override
	public ShortRange duplicate() {
		return new ShortRange(this);
	}
}