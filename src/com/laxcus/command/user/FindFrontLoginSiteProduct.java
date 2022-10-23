/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.user;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 查询FRONT用户的登录站点执行结果
 * 
 * @author scott.liang
 * @version 1.0 2/15/2017
 * @since laxcus 1.0
 */
public class FindFrontLoginSiteProduct extends EchoProduct {
	
	private static final long serialVersionUID = -7284633252471158299L;
	
	/** GATE网关站点（外部地址） **/
	private Node site;

	/**
	 * 生成查询FRONT用户的登录站点执行结果数据副本
	 * @param that FindFrontLoginSiteProduct实例
	 */
	private FindFrontLoginSiteProduct(FindFrontLoginSiteProduct that) {
		super(that);
		site = that.site;
	}
	
	/**
	 * 构造查询FRONT用户的登录站点执行结果
	 */
	public FindFrontLoginSiteProduct() {
		super();
	}

	/**
	 * 构造查询FRONT用户的登录站点执行结果，指定GATE站点
	 * @param e GATE站点
	 */
	public FindFrontLoginSiteProduct(Node e) {
		this();
		setSite(e);
	}

	/**
	 * 从可类化读取器中解析查询FRONT用户的登录站点执行结果
	 * @param reader 可类化读取器
	 */
	public FindFrontLoginSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置GATE站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		site = e;
	}

	/**
	 * 返回GATE站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public FindFrontLoginSiteProduct duplicate() {
		return new FindFrontLoginSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = reader.readInstance(Node.class);
	}

}
