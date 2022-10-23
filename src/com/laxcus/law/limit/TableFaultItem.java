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
 * 数据表锁定单元
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class TableFaultItem extends FaultItem {
	
	private static final long serialVersionUID = 1603458361195426825L;
	
	/** 数据表名称 **/
	private Space space;

	/**
	 * 构造默认和私有的数据表锁定单元
	 */
	private TableFaultItem() {
		super();
	}

	/**
	 * 生成数据表锁定单元数据副本
	 * @param that TableFaultItem实例
	 */
	private TableFaultItem(TableFaultItem that) {
		super(that);
		space = that.space.duplicate();
	}

	/**
	 * 构造数据表锁定单元，指定参数
	 * @param space 表名
	 */
	public TableFaultItem(Space space) {
		super(LawRank.TABLE);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据表锁定单元
	 * @param reader 可类化数据读取器
	 */
	public TableFaultItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出数据表锁定单元
	 * @param reader 标记化读取器
	 */
	public TableFaultItem(MarkReader reader) {
		this();
		reader.readObject(this);
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

	/**
	 * 返回数据库名
	 * @return 数据库名
	 */
	public Fame getFame() {
		return space.getSchema();
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
	public int compareTo(FaultItem that) {
		// 去上级比较
		int ret = super.compareTo(that);
		// 本层比较
		if (ret == 0) {
			TableFaultItem item = (TableFaultItem) that;
			ret = Laxkit.compareTo(space, item.space);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#duplicate()
	 */
	@Override
	public TableFaultItem duplicate() {
		return new TableFaultItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
	}

}