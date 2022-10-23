/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询账号所在的ACCOUNT站点反馈结果。
 * HASH站点发出，GATE/BANK接收。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeAccountSiteProduct extends EchoProduct {

	private static final long serialVersionUID = 6738948156054497065L;
	
	/** 用户签名 **/
	private Siger siger;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 构造默认的查询账号所在的ACCOUNT站点命令
	 */
	private TakeAccountSiteProduct() {
		super();
	}

	/**
	 * 构造查询账号所在的ACCOUNT站点，指定ACCOUNT站点地址
	 * @param siger 用户签名
	 * @param remote ACCOUNT站点地址
	 */
	public TakeAccountSiteProduct(Siger siger, Node remote) {
		this();
		setSiger(siger);
		setRemote(remote);
	}

	/**
	 * 生成查询账号所在的ACCOUNT站点的数据副本
	 * @param that 查询账号所在的ACCOUNT站点
	 */
	private TakeAccountSiteProduct(TakeAccountSiteProduct that) {
		super(that);
		siger = that.siger;
		remote = that.remote;
	}

	/**
	 * 从可类化数据读取器中解析查询账号所在的ACCOUNT站点
	 * @param reader 可类化数据读取器
	 */
	public TakeAccountSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名，允许空指针
	 * @param e
	 */
	public void setSiger(Siger e) {
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 设置ACCOUNT站点地址，允许空指针
	 * @param e ACCOUNT站点地址
	 */
	public void setRemote(Node e) {
		remote = e;
	}

	/**
	 * 返回ACCOUNT站点地址
	 * @return ACCOUNT站点地址
	 */
	public Node getRemote() {
		return remote;
	}
	
	/**
	 * 判断成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return remote != null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeAccountSiteProduct duplicate() {
		return new TakeAccountSiteProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(siger);
		writer.writeInstance(remote);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		siger = reader.readInstance(Siger.class);
		remote = reader.readInstance(Node.class);
	}

}