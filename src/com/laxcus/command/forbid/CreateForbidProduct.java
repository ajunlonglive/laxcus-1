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
 * 提交禁止操作单元处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public class CreateForbidProduct extends DefaultForbidProduct {

	private static final long serialVersionUID = 7224409478938011957L;

	/**
	 * 构造默认的提交禁止操作单元处理结果
	 */
	public CreateForbidProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析提交禁止操作单元处理结果
	 * @param reader - 可类化数据读取器
	 */
	public CreateForbidProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成提交禁止操作单元处理结果数据副本
	 * @param that CreateForbidProduct实例
	 */
	private CreateForbidProduct(CreateForbidProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateForbidProduct duplicate() {
		return new CreateForbidProduct(this);
	}

}