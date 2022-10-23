/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.find;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询数据表站点命令。
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public final class FindTableSite extends FindSite {

	private static final long serialVersionUID = 8077426601971199122L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 构造默认和私有的查询数据表站点命令
	 */
	private FindTableSite() {
		super();
	}

	/**
	 * 根据传入的查询数据表站点命令，生成它的数据副本
	 * @param that FindTableSite实例
	 */
	private FindTableSite(FindTableSite that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造查询数据表站点命令，指定查找站点标识和数据表名
	 * @param tag 查找站点标识
	 * @param space 数据表名
	 */
	public FindTableSite(FindSiteTag tag, Space space) {
		this();
		setTag(tag);
		setSpace(space);
	}

	/**
	 * 构造查询数据表站点命令，指定站点类型
	 * @param family 站点类型
	 */
	public FindTableSite(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造查询数据表站点命令，指定参数
	 * @param family 站点类型
	 * @param space 数据表名
	 */
	public FindTableSite(byte family, Space space) {
		this(family);
		setSpace(space);
	}

	/**
	 * 构造查询数据表站点命令，指定参数
	 * @param family 站点类型
	 * @param rank 站点级别
	 * @param space 数据表名
	 */
	public FindTableSite(byte family, byte rank, Space space) {
		this(family, space);
		setRank(rank);
	}

	/**
	 * 从可类化读取器中解析查找数据表名命令。
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FindTableSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名，不允许空指针
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindTableSite duplicate() {
		return new FindTableSite(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSite#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 数据表名
		writer.writeInstance(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSite#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 数据表名
		space = reader.readInstance(Space.class);
	}

}
