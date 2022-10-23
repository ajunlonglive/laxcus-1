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

/**
 * 查找用户签名站点命令
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public final class FindUserSite extends FindSite {

	private static final long serialVersionUID = 3540434263134413145L;

	/** 用户签名 **/
	private Siger siger;

	/**
	 * 构造默认和私有的查询用户签名站点命令
	 */
	private FindUserSite() {
		super();
	}

	/**
	 * 根据传入的查询用户签名站点命令，生成它的数据副本
	 * @param that FindUserSite实例
	 */
	private FindUserSite(FindUserSite that) {
		super(that);
		siger = that.siger;
	}

	/**
	 * 构造查询用户签名站点命令，指定查找站点标识和用户签名
	 * @param tag 查找站点标识
	 * @param siger 用户签名
	 */
	public FindUserSite(FindSiteTag tag, Siger siger) {
		this();
		setTag(tag);
		setUsername(siger);
	}

	/**
	 * 从可类化读取器中解析查找用户签名命令。
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FindUserSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名，不允许空值
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return siger;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindUserSite duplicate() {
		return new FindUserSite(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSite#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 用户签名
		writer.writeObject(siger);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSite#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 用户签名
		siger = new Siger(reader);
	}

}