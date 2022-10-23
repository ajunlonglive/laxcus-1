/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import com.laxcus.access.schema.*;
import com.laxcus.law.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 行锁定单元
 * 
 * @author scott.liang
 * @version 1.0 4/9/2018
 * @since laxcus 1.0
 */
public final class RowFaultItem extends FaultItem {
	
	private static final long serialVersionUID = 2581210748394108605L;

	/** 行记录特称 **/
	private RowFeature feature;

	/**
	 * 构造默认和私有的行锁定单元
	 */
	private RowFaultItem() {
		super();
	}

	/**
	 * 生成行锁定单元数据副本
	 * @param that RowFaultItem实例
	 */
	private RowFaultItem(RowFaultItem that) {
		super(that);
		feature = that.feature;
	}

	/**
	 * 构造行锁定单元，指定参数
	 * @param feature 行记录特称
	 */
	public RowFaultItem(RowFeature feature) {
		super(LawRank.ROW);
		setFeature(feature);
	}

	/**
	 * 从可类化数据读取器中解析行锁定单元
	 * @param reader 可类化数据读取器
	 */
	public RowFaultItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出行锁定单元
	 * @param reader 标记化读取器
	 */
	public RowFaultItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置行记录特称
	 * @param e 行记录特称
	 */
	public void setFeature(RowFeature e) {
		Laxkit.nullabled(e);

		feature = e;
	}

	/**
	 * 返回行记录特称
	 * @return 行记录特称
	 */
	public RowFeature getFeature() {
		return feature;
	}

	/**
	 * 返回数据库名
	 * @return 数据库名
	 */
	public Fame getFame() {
		return feature.getFame();
	}
	
	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return feature.getSpace();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ feature.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", super.toString(), feature);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FaultItem that) {
		// 去上级比较
		int ret = super.compareTo(that);
		// 本层比较
		if (ret == 0) {
			RowFaultItem item = (RowFaultItem) that;
			ret = Laxkit.compareTo(feature, item.feature);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#duplicate()
	 */
	@Override
	public RowFaultItem duplicate() {
		return new RowFaultItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(feature);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		feature = new RowFeature(reader);
	}

}