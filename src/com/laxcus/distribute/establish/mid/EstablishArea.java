/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import com.laxcus.distribute.mid.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据构建区。<br>
 * 
 * 数据构建区在DATA/BUILD站点的数据构建过程中产生，每个数据构建区，可以包含任意多个数据构建域（EstablishField），
 * 在数据构建过程中，一个DATA/BUILD站点只能产生一个数据构建区。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public abstract class EstablishArea extends SiteArea implements Comparable<EstablishArea> {

	private static final long serialVersionUID = 5231905219380228311L;

	/**
	 * 根据传入的数据构建区域，构造它的浅层数据副本
	 * @param that - 数据构建区
	 */
	protected EstablishArea(EstablishArea that) {
		super(that);
	}

	/**
	 * 生成默认的数据构建区
	 */
	protected EstablishArea() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != this.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return this.compareTo((EstablishArea) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSource().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EstablishArea that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(getSource(), that.getSource());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", getSource(), this.getClass().getSimpleName());
	}

	/**
	 * 将子类参数写入可类化存储器
	 * 
	 * @param writer - 可类化存储器
	 */
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/**
	 * 从可类化读取器中解析子类参数
	 * 
	 * @param reader - 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}
}