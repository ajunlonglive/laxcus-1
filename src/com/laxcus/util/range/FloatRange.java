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
 * 单浮点数范围。
 * 
 * @author scott.liang
 * @version 1.0 1/22/2009
 * @since laxcus 1.0
 */
public final class FloatRange extends Range implements Comparable<FloatRange> {

	private static final long serialVersionUID = 398655036056590674L;

	/** 单浮点值范围 */
	private float begin, end;
	
	/**
	 * 将单浮点范围写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeFloat(this.begin);
		writer.writeFloat(this.end);
		return writer.size() - scale;
	}
	
	/**
	 * 从可类化读取器中解析单浮点范围
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		float s1 = reader.readFloat();
		float s2 = reader.readFloat();
		this.set(s1, s2);
		return reader.getSeek() - scale;
	}


	/**
	 * 使用传入的单浮点范围对象，生成它的副本
	 * @param that
	 */
	private FloatRange(FloatRange that) {
		super(that);
		this.begin = that.begin;
		this.end = that.end;
	}
	
	/**
	 * 构造一个默认的单浮点范围
	 */
	public FloatRange() {
		super();
		begin = end = 0.0f;
	}

	/**
	 * 构造单浮点范围，并且指定它的开始和结束位置
	 * @param begin
	 * @param end
	 */
	public FloatRange(float begin, float end) {
		this();
		this.set(begin, end);
	}
	
	/**
	 * 从可类化读取器中解析单浮点范围
	 * @param reader
	 */
	public FloatRange(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 设置数据范围，开始位置必须小于等于结束位置。否则抛出"算术异常"
	 * @param b
	 * @param e
	 * @throws ArithmeticException
	 */
	public void set(float b, float e) {
		if (b > e) {
			String s = String.format("range out! %f > %f", b, e);
			throw new ArithmeticException(s);
		}
		begin = b;
		end = e;
	}

	/**
	 * 返回开始点
	 * @return
	 */
	public float begin() {
		return this.begin;
	}

	/**
	 * 返回结束点
	 * @return
	 */
	public float end() {
		return this.end;
	}

	/**
	 * 传入值是否在范围内
	 * @param value
	 * @return
	 */
	public boolean inside(float value) {
		return begin <= value && value <= end;
	}

	/*
	 * 检查两个单浮点对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FloatRange.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return this.compareTo((FloatRange) that) == 0;
	}

	/*
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (Float.floatToIntBits(begin) ^ Float.floatToIntBits(end));
	}

	/*
	 * 返回单浮点范围描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%f - %f", this.begin, this.end);
	}

	/*
	 * 比较两个单浮点范围的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FloatRange that) {
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
	public FloatRange duplicate() {
		return new FloatRange(this);
	}
}