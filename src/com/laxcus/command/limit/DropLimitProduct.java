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
 * 删除限制操作规则处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/23/2017
 * @since laxcus 1.0
 */
public class DropLimitProduct extends DefaultLimitProduct {

	private static final long serialVersionUID = 6835921549837109244L;

	/**
	 * 构造默认的删除限制操作规则处理结果
	 */
	public DropLimitProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析删除限制操作规则处理结果
	 * @param reader 可类化数据读取器
	 */
	public DropLimitProduct(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 生成删除限制操作规则处理结果数据副本
	 * @param that DropLimitProduct实例
	 */
	private DropLimitProduct(DropLimitProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DropLimitProduct duplicate() {
		return new DropLimitProduct(this);
	}

}