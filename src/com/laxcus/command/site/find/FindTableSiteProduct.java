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
 * 数据表站点查询结果
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public class FindTableSiteProduct extends FindSiteProduct {

	private static final long serialVersionUID = 1615972837763481222L;
	
	/** 数据表名 **/
	private Space space;

	/**
	 * 使用传入实例，生成数据表站点查询结果的数据副本
	 * @param that FindTableSiteProduct实例
	 */
	private FindTableSiteProduct(FindTableSiteProduct that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造默认的数据表站点查询结果
	 */
	private FindTableSiteProduct() {
		super();
	}

	/**
	 * 构造阶段命名站点查询结果，指定标识
	 * @param tag 查询站点标识
	 */
	public FindTableSiteProduct(FindSiteTag tag) {
		this();
		setTag(tag);
	}

	/**
	 * 构造阶段命名站点查询结果，指定参数
	 * @param tag 查询站点标识
	 * @param space 数据表名
	 */
	public FindTableSiteProduct(FindSiteTag tag, Space space) {
		this(tag);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据表站点查询结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindTableSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSiteProduct#duplicate()
	 */
	@Override
	public FindTableSiteProduct duplicate() {
		return new FindTableSiteProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSiteProduct#buildSuffix(com.laxcus.util.ClassWriter)
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
	 * @see com.laxcus.command.site.find.FindSiteProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 数据表名
		space = reader.readInstance(Space.class);
	}

}