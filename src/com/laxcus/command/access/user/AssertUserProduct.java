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
 * 判断用户账号报告
 * 
 * @author scott.liang
 * @version 1.1 6/26/2015
 * @since laxcus 1.0
 */
public final class AssertUserProduct extends ProcessUserProduct {

	private static final long serialVersionUID = 3931627114234073580L;

	/**
	 * 构造默认和私有的判断用户账号报告
	 */
	private AssertUserProduct() {
		super();
	}

	/**
	 * 生成一个判断用户账号报告数据副本
	 * @param that
	 */
	public AssertUserProduct(AssertUserProduct that) {
		super(that);
	}

	/**
	 * 建立判断用户账号报告，指定数据库名
	 * @param siger 用户签名
	 * @param successful 成功标识
	 */
	public AssertUserProduct(Siger siger, boolean successful) {
		this();
		setUsername(siger);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析判断用户账号报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AssertUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public AssertUserProduct duplicate() {
		return new AssertUserProduct(this);
	}

}