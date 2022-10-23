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
 * 建立限制操作规则处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/23/2017
 * @since laxcus 1.0
 */
public class CreateLimitProduct extends DefaultLimitProduct {

	private static final long serialVersionUID = -2607470892486960751L;

	/**
	 * 构造默认的建立限制操作规则处理结果
	 */
	public CreateLimitProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析建立限制操作规则处理结果
	 * @param reader 可类化数据读取器
	 */
	public CreateLimitProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成建立限制操作规则处理结果数据副本
	 * @param that CreateLimitProduct实例
	 */
	private CreateLimitProduct(CreateLimitProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateLimitProduct duplicate() {
		return new CreateLimitProduct(this);
	}

}
