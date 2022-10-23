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
 * 用户签名站点查询结果
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public class FindUserSiteProduct extends FindSiteProduct {

	private static final long serialVersionUID = 6401423108891130134L;

	/** 用户签名 **/
	private Siger siger;

	/**
	 * 使用传入实例，生成用户签名站点查询结果的数据副本
	 * @param that FindUserSiteProduct实例
	 */
	private FindUserSiteProduct(FindUserSiteProduct that) {
		super(that);
		siger = that.siger;
	}

	/**
	 * 构造默认的用户签名站点查询结果
	 */
	private FindUserSiteProduct() {
		super();
	}

	/**
	 * 构造用户签名站点查询结果，指定标识
	 * @param tag 查询站点标识
	 */
	public FindUserSiteProduct(FindSiteTag tag) {
		this();
		setTag(tag);
	}

	/**
	 * 构造用户签名站点查询结果，指定参数
	 * @param tag 查询站点标识
	 * @param siger 用户签名
	 */
	public FindUserSiteProduct(FindSiteTag tag, Siger siger) {
		this(tag);
		setUsername(siger);
	}

	/**
	 * 从可类化数据读取器中解析用户签名站点查询结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindUserSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSiteProduct#duplicate()
	 */
	@Override
	public FindUserSiteProduct duplicate() {
		return new FindUserSiteProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.site.find.FindSiteProduct#buildSuffix(com.laxcus.util.ClassWriter)
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
	 * @see com.laxcus.command.site.find.FindSiteProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 用户签名
		siger = new Siger(reader);
	}

}