/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.limit;

import com.laxcus.util.classable.*;

/**
 * 撤销锁定单元处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/23/2017
 * @since laxcus 1.0
 */
public class DropFaultProduct extends DefaultLimitProduct {

	private static final long serialVersionUID = -9192637048824207607L;

	/**
	 * 构造默认的撤销锁定单元处理结果
	 */
	public DropFaultProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析撤销锁定单元处理结果
	 * @param reader 可类化数据读取器
	 */
	public DropFaultProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成撤销锁定单元处理结果数据副本
	 * @param that DropFaultProduct实例
	 */
	private DropFaultProduct(DropFaultProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DropFaultProduct duplicate() {
		return new DropFaultProduct(this);
	}

}