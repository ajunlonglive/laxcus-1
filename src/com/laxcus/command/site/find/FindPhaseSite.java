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
 * 查找阶段命名站点命令
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public final class FindPhaseSite extends FindSite {

	private static final long serialVersionUID = 6632313803844144649L;

	/** 阶段命名 **/
	private Phase phase;

	/**
	 * 构造默认和私有的查询阶段命名站点命令
	 */
	private FindPhaseSite() {
		super();
	}

	/**
	 * 根据传入的查询阶段命名站点命令，生成它的数据副本
	 * @param that FindPhaseSite实例
	 */
	private FindPhaseSite(FindPhaseSite that) {
		super(that);
		phase = that.phase;
	}

	/**
	 * 构造查询阶段命名站点命令，指定查找站点标识和阶段命名
	 * @param tag 查找站点标识
	 * @param phase 阶段命名
	 */
	public FindPhaseSite(FindSiteTag tag, Phase phase) {
		this();
		setTag(tag);
		setPhase(phase);
	}

	/**
	 * 构造查询阶段命名站点命令，指定站点类型
	 * @param family 站点类型
	 */
	public FindPhaseSite(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造查询阶段命名站点命令，指定参数
	 * @param family 站点类型
	 * @param phase 阶段命名
	 */
	public FindPhaseSite(byte family, Phase phase) {
		this(family);
		setPhase(phase);
	}

	/**
	 * 构造查询阶段命名站点命令，指定参数
	 * @param family 站点类型
	 * @param rank 站点级别
	 * @param phase 阶段命名
	 */
	public FindPhaseSite(byte family, byte rank, Phase phase) {
		this(family, phase);
		setRank(rank);
	}

	/**
	 * 从可类化读取器中解析查找阶段命名命令。
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FindPhaseSite(ClassReader reader) {
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

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindPhaseSite duplicate() {
		return new FindPhaseSite(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSite#buildSuffix(com.laxcus.util.ClassWriter)
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
	 * @see com.laxcus.command.site.find.FindSite#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 阶段命名
		phase = reader.readInstance(Phase.class);
	}

}