/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 申请主机序列号BANK站点反馈结果。
 * 由BANK节点发出。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeSiteSerialProduct extends EchoProduct {

	private static final long serialVersionUID = 6738948156054497065L;

	/** 反馈编号 **/
	private int no;

	/**
	 * 构造默认的申请主机序列号命令
	 */
	private TakeSiteSerialProduct() {
		super();
		setNo(-1);
	}

	/**
	 * 构造申请主机序列号，指定反馈编号
	 * @param no 反馈编号
	 */
	public TakeSiteSerialProduct(int no) {
		this();
		setNo(no);
	}

	/**
	 * 生成申请主机序列号的数据副本
	 * @param that 申请主机序列号
	 */
	private TakeSiteSerialProduct(TakeSiteSerialProduct that) {
		super(that);
		no = that.no;
	}

	/**
	 * 从可类化数据读取器中解析申请主机序列号
	 * @param reader 可类化数据读取器
	 */
	public TakeSiteSerialProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置反馈编号
	 * @param who 反馈编号
	 */
	public void setNo(int who) {
		no = who;
	}

	/**
	 * 返回反馈编号
	 * @return 反馈编号
	 */
	public int getNo() {
		return no;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeSiteSerialProduct duplicate() {
		return new TakeSiteSerialProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(no);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		no = reader.readInt();
	}

}