/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.zone;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 单浮点索引区域。
 * 
 * @author scott.liang
 * @version 1.2 12/06/2015
 * @since laxcus 1.0
 */
public final class FloatZone extends IndexZone implements Comparable<FloatZone> {

	private static final long serialVersionUID = -1463884408690163302L;

	/** 数据值分布范围 **/
	private FloatRange range;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexZone#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(range);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexZone#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		range = new FloatRange(reader);
	}
	
	/**
	 * 构造默认和私有的FloatZone实例
	 */
	private FloatZone() {
		super(IndexZoneTag.FLOAT_ZONE);
	}
	
	/**
	 * 根据传入的参数，建立一个它的副本
	 * @param that FloatZone实例
	 */
	private FloatZone(FloatZone that) {
		super(that);
		setRange(that.range);
	}
	
	/**
	 * 从可类化读取器中解析FloatZone数据
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FloatZone(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造单浮点索引区域，并且指定它的范围和权重
	 * @param range 范围
	 * @param weight 权重
	 */
	public FloatZone(FloatRange range, int weight) {
		this();
		setRange(range);
		super.setWeight(weight);
	}

	/**
	 * 构造单浮点索引区域，指定全部参数
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @param weight 权重
	 */
	public FloatZone(float begin, float end, int weight) {
		this(new FloatRange(begin, end), weight);
	}

	/**
	 * 返回单浮点范围
	 * @return 单浮点范围实例
	 */
	public FloatRange getRange() {
		return range;
	}

	/**
	 * 设置单浮点范围
	 * @param e 单浮点范围实例
	 */
	public void setRange(FloatRange e) {
		range = e.duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %d", range, getWeight());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FloatZone.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FloatZone) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return range.hashCode() ^ super.getWeight();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FloatZone that) {
		if(that == null) {
			return 1;
		}
		int ret = range.compareTo(that.range);
		if(ret == 0) {
			ret = Laxkit.compareTo(getWeight(), that.getWeight());
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexZone#duplicate()
	 */
	@Override
	public FloatZone duplicate() {
		return new FloatZone(this);
	}
}