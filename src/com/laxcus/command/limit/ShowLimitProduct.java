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
 * 显示限制操作单元处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class ShowLimitProduct extends DefaultLimitProduct {

	private static final long serialVersionUID = 9216308477400322786L;

	/**
	 * 构造默认的显示限制操作单元处理结果
	 */
	public ShowLimitProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示限制操作单元处理结果
	 * @param reader 可类化数据读取器
	 */
	public ShowLimitProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成显示限制操作单元处理结果数据副本
	 * @param that ShowLimitProduct实例
	 */
	private ShowLimitProduct(ShowLimitProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ShowLimitProduct duplicate() {
		return new ShowLimitProduct(this);
	}

}