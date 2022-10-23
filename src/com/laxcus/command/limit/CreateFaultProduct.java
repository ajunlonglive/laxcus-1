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
 * 提交锁定单元处理结果
 * 
 * @author scott.liang
 * @version 1.0 3/23/2017
 * @since laxcus 1.0
 */
public class CreateFaultProduct extends DefaultLimitProduct {

	private static final long serialVersionUID = 7224409478938011957L;

	/**
	 * 构造默认的提交锁定单元处理结果
	 */
	public CreateFaultProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析提交锁定单元处理结果
	 * @param reader 可类化数据读取器
	 */
	public CreateFaultProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成提交锁定单元处理结果数据副本
	 * @param that CreateFaultProduct实例
	 */
	private CreateFaultProduct(CreateFaultProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateFaultProduct duplicate() {
		return new CreateFaultProduct(this);
	}

}