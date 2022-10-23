/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.forbid;

import com.laxcus.util.classable.*;

/**
 * 撤销禁止操作单元处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public class DropForbidProduct extends DefaultForbidProduct {

	private static final long serialVersionUID = -9192637048824207607L;

	/**
	 * 构造默认的撤销禁止操作单元处理结果
	 */
	public DropForbidProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析撤销禁止操作单元处理结果
	 * @param reader 可类化数据读取器
	 */
	public DropForbidProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成撤销禁止操作单元处理结果数据副本
	 * @param that DropForbidProduct实例
	 */
	private DropForbidProduct(DropForbidProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DropForbidProduct duplicate() {
		return new DropForbidProduct(this);
	}

}