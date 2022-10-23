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
 * 刷新元数据结果
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public final class RefreshMetadataProduct extends RefreshResourceProduct {
	
	private static final long serialVersionUID = -8446250933628748609L;

	/**
	 * 构造默认的刷新元数据结果
	 */
	public RefreshMetadataProduct() {
		super();
	}

	/**
	 * 根据传入的刷新元数据结果，生成它的数据副本
	 * @param that
	 */
	private RefreshMetadataProduct(RefreshMetadataProduct that) {
		super(that);
	}

	/**
	 * 从可类化数据读取器中解析刷新元数据结果
	 * @param reader 可类化数据读取器
	 */
	public RefreshMetadataProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public RefreshMetadataProduct duplicate() {
		return new RefreshMetadataProduct(this);
	}

}