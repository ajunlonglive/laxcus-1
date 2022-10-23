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
 * 双浮点索引分布区域
 * 
 * @author scott.liang
 * @version 1.2 12/06/2015
 * @since laxcus 1.0
 */
public final class DoubleZone extends IndexZone implements Comparable<DoubleZone> {

	private static final long serialVersionUID = 344990415544211127L;

	/** 数据值分布范围 **/
	private DoubleRange range;

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
		range = new DoubleRange(reader);
	}

	/**
	 * 构造默认和私有的DoubleZone实例
	 */
	private DoubleZone() {
		super(IndexZoneTag.DOUBLE_ZONE);
	}

	/**
	 * 根据传入的参数，建立一个它的副本
	 * @param that DoubleZone实例
	 */
	private DoubleZone(DoubleZone that) {
		super(that);
		setRange(that.range);
	}
	
	/**
	 * 从可类化读取器中解析双浮点索引区域
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public DoubleZone(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 构造双浮点索引区域，并且指定它的范围和权重
	 * @param range 双浮点范围
	 * @param weight 权重
	 */
	public DoubleZone(DoubleRange range, int weight) {
		this();
		setRange(range);
		setWeight(weight);
	}

	/**
	 * 构造双浮点索引区域，指定全部参数
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @param weight 权重
	 */
	public DoubleZone(double begin, double end, int weight) {
		this(new DoubleRange(begin, end), weight);
	}

	/**
	 * 返回双浮点范围
	 * @return 双浮点范围实例
	 */
	public DoubleRange getRange() {
		return range;
	}

	/**
	 * 设置双浮点范围
	 * @param e 双浮点范围实例
	 * @throws NullPointerException，如果实例是空指针
	 */
	public void setRange(DoubleRange e) {
		Laxkit.nullabled(e);

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
		if (that == null || that.getClass() != DoubleZone.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((DoubleZone) that) == 0;
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
	public int compareTo(DoubleZone that) {
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
	public DoubleZone duplicate() {
		return new DoubleZone(this);
	}
}