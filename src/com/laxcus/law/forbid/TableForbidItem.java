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
 * 数据表级禁止操作单元
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public final class TableForbidItem extends ForbidItem {
	
	private static final long serialVersionUID = 8998048517556214615L;
	
	/** 数据表名 **/
	private Space space;

	/**
	 * 构造默认和私有的数据表级禁止操作单元
	 */
	private TableForbidItem() {
		super();
	}

	/**
	 * 生成数据表级禁止操作单元数据副本
	 * @param that TableForbidItem实例
	 */
	private TableForbidItem(TableForbidItem that) {
		super(that);
		space = that.space.duplicate();
	}

	/**
	 * 构造数据表级禁止操作单元，指定参数
	 * @param space 数据表名
	 */
	public TableForbidItem(Space space) {
		super(LawRank.TABLE);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据表级禁止操作单元
	 * @param reader 可类化数据读取器
	 */
	public TableForbidItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() ^ space.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", super.toString(), space);
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
			TableForbidItem item = (TableForbidItem) that;
			ret = Laxkit.compareTo(space, item.space);
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
			success = (Laxkit.compareTo(space.getSchema(), item.getFame()) == 0);
		} else if (that.getClass() == TableForbidItem.class) {
			TableForbidItem item = (TableForbidItem) that;
			success = (Laxkit.compareTo(space, item.space) == 0);
		} else if (that.getClass() == RowForbidItem.class) {
			RowForbidItem item = (RowForbidItem) that;
			success = (Laxkit.compareTo(space, item.getFeature().getSpace()) == 0);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#duplicate()
	 */
	@Override
	public ForbidItem duplicate() {
		return new TableForbidItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.policy.forbid.ForbidItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}

}