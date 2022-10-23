/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 判断表存在报告
 * 
 * @author scott.liang
 * @version 1.1 8/1/2015
 * @since laxcus 1.0
 */
public class AssertTableProduct extends DefaultTableProduct {

	private static final long serialVersionUID = 3165391173133661066L;

	/**
	 * 构造默认的判断表存在报告
	 */
	private AssertTableProduct() {
		super();
	}

	/**
	 * 生成一个判断表存在报告数据副本
	 * @param that AssertTableProduct实例
	 */
	private AssertTableProduct(AssertTableProduct that) {
		super(that);
	}

	/**
	 * 建立判断用户账号报告，指定表名和成功标识
	 * @param space 表名
	 * @param successful 成功标识
	 */
	public AssertTableProduct(Space space, boolean successful) {
		this();
		setSpace(space);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析判断表存在报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AssertTableProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public AssertTableProduct duplicate() {
		return new AssertTableProduct(this);
	}

}