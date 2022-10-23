/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.forbid;

import com.laxcus.access.schema.*;
import com.laxcus.law.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据库级禁止操作单元
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public final class SchemaForbidItem extends ForbidItem {
	
	private static final long serialVersionUID = -7881459416775822577L;
	
	/** 数据库名称 **/
	private Fame fame;

	/**
	 * 构造默认和私有的数据库级禁止操作单元
	 */
	private SchemaForbidItem() {
		super();
	}

	/**
	 * 生成数据库级禁止操作单元数据副本
	 * @param that SchemaForbidItem实例
	 */
	private SchemaForbidItem(SchemaForbidItem that) {
		super(that);
		fame = that.fame;
	}

	/**
	 * 构造数据库级禁止操作单元，指定参数
	 * @param fame 数据库名
	 */
	public SchemaForbidItem( Fame fame) {
		super(LawRank.SCHEMA);
		setFame(fame);
	}

	/**
	 * 从可类化数据读取器中解析数据库级禁止操作单元
	 * @param reader 可类化数据读取器
	 */
	public SchemaForbidItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据库名
	 * @param e Fame实例
	 */
	public void setFame(Fame e) {
		Laxkit.nullabled(e);

		fame = e;
	}

	/**
	 * 返回数据库名
	 * @return Fame实例
	 */
	public Fame getFame() {
		return fame;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ fame.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", super.toString(), fame);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ForbidItem that) {
		// 去上级比较
		int ret = super.compareTo(that);
		// 比较数据库
		if (ret == 0) {
			SchemaForbidItem item = (SchemaForbidItem) that;
			ret = Laxkit.compareTo(fame, item.fame);
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
			success = (Laxkit.compareTo(fame, item.fame) == 0);
		} else if (that.getClass() == TableForbidItem.class) {
			TableForbidItem item = (TableForbidItem) that;
			success = (Laxkit.compareTo(fame, item.getSpace().getSchema()) == 0);
		} else if (that.getClass() == RowForbidItem.class) {
			RowForbidItem item = (RowForbidItem) that;
			success = (Laxkit.compareTo(fame, item.getFeature().getFame()) == 0);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#duplicate()
	 */
	@Override
	public ForbidItem duplicate() {
		return new SchemaForbidItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(fame);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		fame = new Fame(reader);
	}

}