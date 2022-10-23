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
 * 短整型有效范围分布区域。<br>
 * 
 * @author scott.liang
 * @version 1.2 12/06/2015
 * @since laxcus 1.0
 */
public final class ShortZone extends IndexZone implements Comparable<ShortZone> {

	private static final long serialVersionUID = 1920336262401568735L;

	/** 短整型(SHORT)索引分布范围 **/
	private ShortRange range;

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
		range = new ShortRange(reader);
	}

	/**
	 * 构造默认和私有的ShortZone实例
	 */
	private ShortZone() {
		super(IndexZoneTag.SHORT_ZONE);
	}

	/**
	 * 根据传入参数生成对象副本
	 * @param that ShortZone实例
	 */
	private ShortZone(ShortZone that) {
		super(that);
		setRange(that.range);
	}

	/**
	 * 从可类化读取器中解析短整形索引区域
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public ShortZone(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 构造短整形索引区域，并且指定它的范围和权重
	 * @param range 短整形范围
	 * @param weight 权重
	 */
	public ShortZone(ShortRange range, int weight) {
		this();
		setRange(range);
		setWeight(weight);
	}

	/**
	 * 构造短整型索引区域，指定全部参数
	 * @param begin 开始位置
	 * @param end 结束位置
	 * @param weight 权重
	 */
	public ShortZone(short begin, short end, int weight) {
		this(new ShortRange(begin, end), weight);
	}

	/**
	 * 返回短整形范围
	 * @return 短整形范围实例
	 */
	public ShortRange getRange() {
		return range;
	}

	/**
	 * 设置短整形范围
	 * @param e 短整形范围实例
	 * @throws NullPointerException，如果实例是空指针
	 */
	public void setRange(ShortRange e) {
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
		if (that == null || that.getClass() != ShortZone.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ShortZone) that) == 0;
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
	public int compareTo(ShortZone that) {
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
	 * 根据当前短整型区域实例，生成一个它的副本
	 * @see com.laxcus.access.index.balance.IndexZone#duplicate()
	 */
	@Override
	public ShortZone duplicate() {
		return new ShortZone(this);
	}
}