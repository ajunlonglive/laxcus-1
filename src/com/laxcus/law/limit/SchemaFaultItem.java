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
 * 数据库锁定单元
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class SchemaFaultItem extends FaultItem {
	
	private static final long serialVersionUID = 5687723326828543276L;
	
	/** 数据库名称 **/
	private Fame fame;

	/**
	 * 构造默认和私有的数据库锁定单元
	 */
	private SchemaFaultItem() {
		super();
	}

	/**
	 * 生成数据库锁定单元数据副本
	 * @param that SchemaFaultItem实例
	 */
	private SchemaFaultItem(SchemaFaultItem that) {
		super(that);
		fame = that.fame;
	}

	/**
	 * 构造数据库锁定单元，指定参数
	 * @param fame 数据库名
	 */
	public SchemaFaultItem(Fame fame) {
		super(LawRank.SCHEMA);
		setFame(fame);
	}

	/**
	 * 从可类化数据读取器中解析数据库锁定单元
	 * @param reader 可类化数据读取器
	 */
	public SchemaFaultItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出数据库锁定单元
	 * @param reader 标记化读取器
	 */
	public SchemaFaultItem(MarkReader reader) {
		this();
		reader.readObject(this);
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
	public int compareTo(FaultItem that) {
		// 上级比较
		int ret = super.compareTo(that);
		// 当前比较
		if (ret == 0) {
			SchemaFaultItem item = (SchemaFaultItem) that;
			ret = Laxkit.compareTo(fame, item.fame);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#duplicate()
	 */
	@Override
	public SchemaFaultItem duplicate() {
		return new SchemaFaultItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(fame);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.account.limit.FaultItem#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		fame = new Fame(reader);
	}

}