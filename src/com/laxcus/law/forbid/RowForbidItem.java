/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.forbid;

import com.laxcus.law.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 行级禁止操作单元
 * 
 * @author scott.liang
 * @version 1.0 4/10/2018
 * @since laxcus 1.0
 */
public final class RowForbidItem extends ForbidItem {

	private static final long serialVersionUID = 6525549356324591381L;

	/** 行记录特称 **/
	private RowFeature feature;

	/**
	 * 构造默认和私有的行级禁止操作单元
	 */
	private RowForbidItem() {
		super();
	}

	/**
	 * 生成行级禁止操作单元数据副本
	 * @param that RowForbidItem实例
	 */
	private RowForbidItem(RowForbidItem that) {
		super(that);
		feature = that.feature;
	}

	/**
	 * 构造行级禁止操作单元，指定参数
	 * @param feature 行记录特称
	 */
	public RowForbidItem(RowFeature feature) {
		super(LawRank.ROW);
		setFeature(feature);
	}

	/**
	 * 从可类化数据读取器中解析行级禁止操作单元
	 * @param reader 可类化数据读取器
	 */
	public RowForbidItem(ClassReader reader) {
		this();
		resolve(reader);
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
	public int compareTo(ForbidItem that) {
		// 去上级比较
		int ret = super.compareTo(that);
		// 比较表名
		if (ret == 0) {
			RowForbidItem item = (RowForbidItem) that;
			ret = Laxkit.compareTo(feature, item.feature);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#conflict(com.laxcus.policy.forbid.ForbidItem)
	 */
	@Override
	public boolean conflict(ForbidItem that) {
		boolean success = false;
		if (that.getClass() == UserForbidItem.class) {
			success = true;
		} else if (that.getClass() == SchemaForbidItem.class) {
			SchemaForbidItem item = (SchemaForbidItem) that;
			success = (Laxkit.compareTo(feature.getFame(), item.getFame()) == 0);
		} else if (that.getClass() == TableForbidItem.class) {
			TableForbidItem item = (TableForbidItem) that;
			success = (Laxkit.compareTo(feature.getSpace(), item.getSpace()) == 0);
		} else if (that.getClass() == RowForbidItem.class) {
			RowForbidItem item = (RowForbidItem) that;
			success = (Laxkit.compareTo(feature, item.feature) == 0);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#duplicate()
	 */
	@Override
	public ForbidItem duplicate() {
		return new RowForbidItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(feature);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		feature = new RowFeature(reader);
	}

}