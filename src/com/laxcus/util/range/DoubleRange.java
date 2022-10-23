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
 * 双浮点范围。
 * 
 * @author scott.liang
 * @version 1.0 1/21/2009
 * @since laxcus 1.0
 */
public final class DoubleRange extends Range implements Comparable<DoubleRange> {

	private static final long serialVersionUID = -8497620933664617708L;

	/** 双浮点数范围 **/
	private double begin, end;
	
	/**
	 * 将双浮点范围写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeDouble(this.begin);
		writer.writeDouble(this.end);
		return writer.size() - scale;
	}
	
	/**
	 * 从可类化读取器中解析双浮点范围
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		double s1 = reader.readDouble();
		double s2 = reader.readDouble();
		this.set(s1, s2);
		return reader.getSeek() - scale;
	}


	/**
	 * 使用传入的双浮点对象，生成它的副本
	 * @param that
	 */
	private DoubleRange(DoubleRange that) {
		super(that);
		this.begin = that.begin;
		this.end = that.end;
	}
	
	/**
	 * 构造一个默认的双浮点范围
	 */
	public DoubleRange() {
		super();
		begin = end = 0.0f;
	}

	/**
	 * 构造双浮点范围，指定它的开始和结束位置
	 * @param begin
	 * @param end
	 */
	public DoubleRange(double begin, double end) {
		this();
		this.set(begin, end);
	}
	
	/**
	 * 从可类化读取器中解析双浮点范围
	 * @param reader
	 */
	public DoubleRange(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 设置数据范围，开始位置必须小于等于结束位置。否则抛出"算术异常"
	 * @param b
	 * @param e
	 * @throws ArithmeticException
	 */
	public void set(double b, double e) {
		if (b > e) {
			String s = String.format("range out! %f > %f", b, e);
			throw new ArithmeticException(s);
		}
		begin = b;
		end = e;
	}

	/**
	 * 返回开始位置
	 * @return
	 */
	public double begin() {
		return this.begin;
	}

	/**
	 * 返回结束位置
	 * @return
	 */
	public double end() {
		return this.end;
	}

	/**
	 * 传入值是否在范围内
	 * @param value
	 * @return
	 */
	public boolean inside(double value) {
		return begin <= value && value <= end;
	}

	/*
	 * 比较两个双浮点对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != DoubleRange.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return this.compareTo((DoubleRange) that) == 0;
	}

	/*
	 * 生成字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%f - %f", this.begin, this.end);
	}

	/*
	 * 生成散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (Double.doubleToLongBits(begin) ^ Double.doubleToLongBits(end));
	}

	/*
	 * 对两个双浮点范围按照数值进行排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DoubleRange that) {
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
	 * 根据当前双浮点实例，生成一个它的副本
	 * @see com.laxcus.util.range.NumberRange#duplicate()
	 */
	@Override
	public DoubleRange duplicate() {
		return new DoubleRange(this);
	}
}