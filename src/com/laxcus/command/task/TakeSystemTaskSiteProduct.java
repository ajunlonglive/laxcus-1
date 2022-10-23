/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 获取保存系统组件的ACCOUNT站点反馈结果
 * 
 * @author scott.liang
 * @version 1.0 10/10/2019
 * @since laxcus 1.0
 */
public class TakeSystemTaskSiteProduct extends EchoProduct {

	private static final long serialVersionUID = -4484266933220870735L;

	/** ACCOUNT节点地址 **/
	private Node site;

	/**
	 * 构造获取保存系统组件的ACCOUNT站点反馈结果
	 */
	public TakeSystemTaskSiteProduct() {
		super();
	}

	/**
	 * 生成获取保存系统组件的ACCOUNT站点反馈结果数据副本
	 * @param that 获取保存系统组件的ACCOUNT站点反馈结果
	 */
	private TakeSystemTaskSiteProduct(TakeSystemTaskSiteProduct that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造获取保存系统组件的ACCOUNT站点反馈结果
	 * @param site 站点地址
	 */
	public TakeSystemTaskSiteProduct(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化读取器中解析获取保存系统组件的ACCOUNT站点反馈结果
	 * @param reader 可类化数据读取器
	 */
	public TakeSystemTaskSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置ACCOUNT站点地址
	 * @param e ACCOUNT站点地址
	 */
	public void setSite(Node e) {
		site = e;
	}

	/**
	 * 返回ACCOUNT站点地址
	 * @return ACCOUNT站点地址
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 判断ACCOUNT地址有效
	 * @return 返回真或者假
	 */
	public boolean hasSite() {
		return site != null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.ConfirmProduct#duplicate()
	 */
	@Override
	public TakeSystemTaskSiteProduct duplicate() {
		return new TakeSystemTaskSiteProduct(this);
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