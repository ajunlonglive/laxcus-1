/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import com.laxcus.access.schema.*;
import com.laxcus.distribute.mid.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据构建段。<br>
 * 
 * 存取ESTABLISH处理过程中产生的元数据。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public abstract class EstablishZone extends MiddleZone implements Comparable<EstablishZone> {

	private static final long serialVersionUID = -2796985286728357318L;

	/** 数据构建标识，由表名和主机地址组成，以此判断它们的唯一性 **/
	private EstablishFlag flag;

	/**
	 * 构造一个默认和私有的数据构建段
	 */
	protected EstablishZone() {
		super();
	}

	/**
	 * 根据传入的数据构建段参数，生成它的数据副本
	 * @param that - 数据构建段实例
	 */
	protected EstablishZone(EstablishZone that) {
		super(that);
		flag = that.flag.duplicate();
	}

	/**
	 * 构造数据构建段，同时设置它的数据表名
	 * @param flag - 数据构建标识
	 */
	protected EstablishZone(EstablishFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 设置数据构建标识
	 * @param e EstablishFlag实例
	 */
	public void setFlag(EstablishFlag e) {
		Laxkit.nullabled(e);

		flag = e;
	}

	/**
	 * 返回数据构建标识
	 * @return EstablishFlag实例
	 */
	public EstablishFlag getFlag() {
		return flag;
	}

	/**
	 * 返回源主机地址
	 * @return Node实例
	 */
	public Node getSource() {
		return flag.getSource();
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return flag.getSpace();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((EstablishZone) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return flag.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EstablishZone that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(flag, that.flag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 数据构建标识
		writer.writeObject(flag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 数据构建标识
		flag = new EstablishFlag(reader);
	}
}