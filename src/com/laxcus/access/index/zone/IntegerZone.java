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
 * 整型值索引分布区域。<br>
 * 
 * @author scott.liang
 * @version 1.2 12/06/2015
 * @since laxcus 1.0
 */
public final class IntegerZone extends IndexZone implements Comparable<IntegerZone> {

	private static final long serialVersionUID = 6601572435194368241L;

	/** 数据值分布范围 **/
	private IntegerRange range;

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
		range = new IntegerRange(reader);
	}

	/**
	 * 构造默认和私有的IntegerZone实例
	 */
	private IntegerZone() {
		super(IndexZoneTag.INTEGER_ZONE);
	}

	/**
	 * 根据整型值索引分布区域，生成它的数据副本
	 * @param that IntegerZone实例
	 */
	private IntegerZone(IntegerZone that) {
		super(that);
		this.setRange(that.range);
	}

	/**
	 * 从可类化读取器中解析整型索引区域
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public IntegerZone(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 构造整型索引区域，并且指定它的范围和权重
	 * @param range 整型范围
	 * @param weight 权重
	 */
	public IntegerZone(IntegerRange range, int weight) {
		this();
		setRange(range);
		setWeight(weight);
	}

	/**
	 * 构造整形索引区域，指定全部参数
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @param weight 权重
	 */
	public IntegerZone(int begin, int end, int weight) {
		this(new IntegerRange(begin, end), weight);
	}

	/**
	 * 返回整形范围
	 * @return 整形范围实例
	 */
	public IntegerRange getRange() {
		return range;
	}

	/**
	 * 设置整形范围
	 * @param e 整形范围实例
	 * @throws NullPointerException，如果实例是空指针
	 */
	public void setRange(IntegerRange e) {
		Laxkit.nullabled(e);

		range = e.duplicate();
	}

	/*
	 * 整型索引范围的分布描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d,%d %d", range.begin(), range.end(), getWeight());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != IntegerZone.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return this.compareTo((IntegerZone) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return range.hashCode() ^ getWeight();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IntegerZone that) {
		// 空值在前
		if(that == null) {
			return 1;
		}
		int ret = this.range.compareTo(that.range);
		if(ret == 0) {
			ret = Laxkit.compareTo(this.getWeight(), that.getWeight());
		}
		return ret;
	}

	/*
	 * 根据当前整型值区域实例，生成它的副本
	 * @see com.laxcus.access.index.balance.IndexZone#duplicate()
	 */
	@Override
	public IntegerZone duplicate() {
		return new IntegerZone(this);
	}
}