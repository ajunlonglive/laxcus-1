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
 * 长整型索引分布区域。<br>
 * 
 * @author scott.liang
 * @version 1.2 12/06/2015
 * @since laxcus 1.0
 */
public final class LongZone extends IndexZone implements Comparable<LongZone> {

	private static final long serialVersionUID = -7823421118101440240L;

	/** 数据值分布范围 **/
	private LongRange range;

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
		range = new LongRange(reader);
	}
	
	/**
	 * 构造默认和私有的LongZone实例
	 */
	private LongZone() {
		super(IndexZoneTag.LONG_ZONE);
	}

	/**
	 * 根据传入的长整型索引分布区域，建立一个它的副本
	 * @param that
	 */
	private LongZone(LongZone that) {
		super(that);
		setRange(that.range);
	}

	/**
	 * 从可类化读取器中解析长整型索引区域
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public LongZone(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 构造长整型索引区域，并且指定它的范围和权重
	 * @param range 长整型范围
	 * @param weight 权重
	 */
	public LongZone(LongRange range, int weight) {
		this();
		setRange(range);
		setWeight(weight);
	}

	/**
	 * 构造长整型索引区域，指定全部参数
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @param weight 权重
	 */
	public LongZone(long begin, long end, int weight) {
		this(new LongRange(begin, end), weight);
	}

	/**
	 * 返回长整形范围
	 * @return 长整形范围实例
	 */
	public LongRange getRange() {
		return range;
	}

	/**
	 * 设置长整形范围
	 * @param e 长整形范围实例
	 * @throws NullPointerException，如果实例是空指针
	 */
	public void setRange(LongRange e) {
		Laxkit.nullabled(e);

		range = e.duplicate();
	}
	
	/*
	 * (non-Javadoc)
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
		if (that == null || that.getClass() != LongZone.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return this.compareTo((LongZone) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.range.hashCode() ^ super.getWeight();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LongZone that) {
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
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexZone#duplicate()
	 */
	@Override
	public LongZone duplicate() {
		return new LongZone(this);
	}
}