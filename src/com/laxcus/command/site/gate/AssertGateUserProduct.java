/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.gate;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 判断账号在GATE站点存在的反馈结果
 * 
 * @author scott.liang
 * @version 1.0 7/18/2019
 * @since laxcus 1.0
 */
public class AssertGateUserProduct extends ConfirmProduct {

	private static final long serialVersionUID = 5090404390661439799L;

	/** 用户签名 **/
	private Siger siger;

	/** GATE站点公网地址 **/
	private Node site;

	/**
	 * 构造默认的判断账号在GATE站点存在的反馈结果
	 */
	public AssertGateUserProduct() {
		super();
	}
	
	/**
	 * 构造判断账号在GATE站点存在的反馈结果，指定参数
	 * @param successful 成功
	 * @param siger 用户签名
	 * @param site GATE站点地址（公网或者内网）
	 */
	public AssertGateUserProduct(boolean successful, Siger siger, Node site) {
		this();
		setSuccessful(successful);
		setSiger(siger);
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中解析判断账号在GATE站点存在的反馈结果
	 * @param reader 可类化数据读取器
	 */
	public AssertGateUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成判断账号在GATE站点存在的反馈结果的数据副本
	 * @param that 判断账号在GATE站点存在的反馈结果
	 */
	private AssertGateUserProduct(AssertGateUserProduct that) {
		super(that);
		siger = that.siger;
		site = that.site;
	}

	/**
	 * 设置用户签名
	 * @param e 用户签名
	 */
	public void setSiger(Siger e) {
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return 用户签名
	 */
	public Siger getSiger() {
		return siger;
	}
	
	/**
	 * 设置GATE公网站点
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		site = e;
	}

	/**
	 * 返回GATE公网站点
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public AssertGateUserProduct duplicate() {
		return new AssertGateUserProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(siger);
		writer.writeInstance(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		siger = reader.readInstance(Siger.class);
		site = reader.readInstance(Node.class);
	}

}