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
 * 显示禁止操作单元处理结果
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */
public class ShowForbidProduct extends DefaultForbidProduct {

	private static final long serialVersionUID = -8047013768285720984L;

	/**
	 * 构造默认的显示禁止操作单元处理结果
	 */
	public ShowForbidProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示禁止操作单元处理结果
	 * @param reader 可类化数据读取器
	 */
	public ShowForbidProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成显示禁止操作单元处理结果数据副本
	 * @param that ShowForbidProduct实例
	 */
	private ShowForbidProduct(ShowForbidProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ShowForbidProduct duplicate() {
		return new ShowForbidProduct(this);
	}

}