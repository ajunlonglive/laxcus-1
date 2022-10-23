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
 * 开放用户账号报告
 * 
 * @author scott.liang
 * @version 1.0 1/4/2020
 * @since laxcus 1.0
 */
public final class OpenUserProduct extends ProcessUserProduct {

	private static final long serialVersionUID = 5888559079014594425L;

	/**
	 * 构造默认和私有的开放用户账号报告
	 */
	private OpenUserProduct() {
		super();
	}

	/**
	 * 生成一个开放用户账号报告数据副本
	 * @param that
	 */
	public OpenUserProduct(OpenUserProduct that) {
		super(that);
	}

	/**
	 * 建立开放用户账号报告，指定数据库名
	 * @param siger 用户签名
	 * @param successful 成功标识
	 */
	public OpenUserProduct(Siger siger, boolean successful) {
		this();
		setUsername(siger);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析开放用户账号报告
	 * @param reader 可类化数据读取器
	 */
	public OpenUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public OpenUserProduct duplicate() {
		return new OpenUserProduct(this);
	}

}