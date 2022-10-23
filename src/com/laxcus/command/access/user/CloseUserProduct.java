/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 禁用用户账号报告
 * 
 * @author scott.liang
 * @version 1.0 1/4/2020
 * @since laxcus 1.0
 */
public final class CloseUserProduct extends ProcessUserProduct {

	private static final long serialVersionUID = 7429782862997833100L;

	/**
	 * 构造默认和私有的禁用用户账号报告
	 */
	private CloseUserProduct() {
		super();
	}

	/**
	 * 生成一个禁用用户账号报告数据副本
	 * @param that
	 */
	public CloseUserProduct(CloseUserProduct that) {
		super(that);
	}

	/**
	 * 建立禁用用户账号报告，指定数据库名
	 * @param siger 用户签名
	 * @param successful 成功标识
	 */
	public CloseUserProduct(Siger siger, boolean successful) {
		this();
		setUsername(siger);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析禁用用户账号报告
	 * @param reader 可类化数据读取器
	 */
	public CloseUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CloseUserProduct duplicate() {
		return new CloseUserProduct(this);
	}

}