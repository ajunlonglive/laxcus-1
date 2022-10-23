/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.classable.*;

/**
 * 刷新注册用户结果
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public final class RefreshUserProduct extends RefreshResourceProduct {
	
	private static final long serialVersionUID = 8390179040754853883L;

	/**
	 * 构造默认的刷新注册用户结果
	 */
	public RefreshUserProduct() {
		super();
	}

	/**
	 * 根据传入的刷新注册用户结果，生成它的数据副本
	 * @param that RefreshUserProduct实例
	 */
	private RefreshUserProduct(RefreshUserProduct that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析刷新注册用户结果
	 * @param reader 可类化数据读取器
	 */
	public RefreshUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public RefreshUserProduct duplicate() {
		return new RefreshUserProduct(this);
	}

}