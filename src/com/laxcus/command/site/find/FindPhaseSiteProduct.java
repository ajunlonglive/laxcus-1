/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.find;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 阶段命名站点查询结果
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public class FindPhaseSiteProduct extends FindSiteProduct {

	private static final long serialVersionUID = 6401423108891130134L;

	/** 阶段命名 **/
	private Phase phase;

	/**
	 * 使用传入实例，生成阶段命名站点查询结果的数据副本
	 * @param that FindPhaseSiteProduct实例
	 */
	private FindPhaseSiteProduct(FindPhaseSiteProduct that) {
		super(that);
		phase = that.phase;
	}

	/**
	 * 构造默认的阶段命名站点查询结果
	 */
	private FindPhaseSiteProduct() {
		super();
	}

	/**
	 * 构造阶段命名站点查询结果，指定标识
	 * @param tag 查询站点标识
	 */
	public FindPhaseSiteProduct(FindSiteTag tag) {
		this();
		setTag(tag);
	}

	/**
	 * 构造阶段命名站点查询结果，指定参数
	 * @param tag 查询站点标识
	 * @param phase 阶段命名
	 */
	public FindPhaseSiteProduct(FindSiteTag tag, Phase phase) {
		this(tag);
		setPhase(phase);
	}

	/**
	 * 从可类化数据读取器中解析阶段命名站点查询结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindPhaseSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置阶段命名
	 * @param e Phase实例
	 */
	public void setPhase(Phase e) {
		Laxkit.nullabled(e);

		phase = e;
	}

	/**
	 * 返回阶段命名
	 * @return Phase实例
	 */
	public Phase getPhase() {
		return phase;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSiteProduct#duplicate()
	 */
	@Override
	public FindPhaseSiteProduct duplicate() {
		return new FindPhaseSiteProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSiteProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 阶段命名
		writer.writeInstance(phase);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSiteProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 阶段命名
		phase = reader.readInstance(Phase.class);
	}

}