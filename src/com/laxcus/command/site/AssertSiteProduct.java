/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询站点存在结果
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public final class AssertSiteProduct extends ConfirmProduct {

	private static final long serialVersionUID = 4282687286401579939L;

	/** 被查询的站点地址 **/
	private Node site;

	/**
	 * 构造默认和私有的查询站点存在结果
	 */
	private AssertSiteProduct() {
		super();
	}

	/**
	 * 生成一个查询站点存在结果数据副本
	 * @param that AssertSiteProduct实例
	 */
	private AssertSiteProduct(AssertSiteProduct that) {
		super(that);
		site = that.site;
	}

	/**
	 * 建立查询站点存在结果，指定参数
	 * @param site 被查询的站点地址
	 * @param successful 成功标识
	 */
	public AssertSiteProduct(Node site, boolean successful) {
		this();
		setSite(site);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析查询站点存在结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AssertSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置被查询的站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回被查询的站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public AssertSiteProduct duplicate() {
		return new AssertSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		site = new Node(reader);
	}
}