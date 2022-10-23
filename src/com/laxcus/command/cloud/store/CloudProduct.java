/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 云服务处理结果
 * 
 * @author scott.liang
 * @version 1.0 4/26/2022
 * @since laxcus 1.0
 */
public abstract class CloudProduct extends EchoProduct {
	
	private static final long serialVersionUID = 1975060572164557553L;

	/** 内网地址 **/
	private Node site;

	/** 网关地址 **/
	private Node gateway;

	/**
	 * 构造默认的云服务处理结果
	 */
	public CloudProduct() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析云服务处理结果
	 * @param reader 可类化读取器
	 */
	public CloudProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成云服务处理结果副本
	 * @param that
	 */
	protected CloudProduct(CloudProduct that) {
		super(that);
		site = that.site;
		gateway = that.gateway;
	}

	/**
	 * 设置节点地址
	 * @param e
	 */
	public void setSite(Node e) {
		site = e;
	}

	/**
	 * 返回节点地址
	 * @return
	 */
	public Node getSite() {
		return site;
	}
	
	/**
	 * 设置网关地址
	 * @param e
	 */
	public void setGateway(Node e) {
		gateway = e;
	}

	/**
	 * 返回网关地址
	 * @return
	 */
	public Node getGateway() {
		return gateway;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
		writer.writeInstance(gateway);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new Node(reader);
		gateway = reader.readInstance(Node.class);
	}

}