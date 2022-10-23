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
 * 删除数据表处理结果
 * 
 * @author scott.liang
 * @version 1.1 6/26/2015
 * @since laxcus 1.0
 */
public final class DropTableProduct extends DefaultTableProduct {

	private static final long serialVersionUID = -5824721526719390946L;

	/**
	 * 构造默认和私有的删除数据表处理结果
	 */
	private DropTableProduct() {
		super();
	}

	/**
	 * 生成一个删除数据表处理结果数据副本
	 * @param that DropTableProduct实例
	 */
	private DropTableProduct(DropTableProduct that) {
		super(that);
	}
	
	/**
	 * 建立删除数据表处理结果，指定数据库名
	 * @param space 数据表名称
	 */
	public DropTableProduct(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 建立删除数据表处理结果，指定数据库名
	 * @param space 数据表名称
	 * @param successful 成功标识
	 */
	public DropTableProduct(Space space, boolean successful) {
		this(space);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析删除数据表处理结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DropTableProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DropTableProduct duplicate() {
		return new DropTableProduct(this);
	}
	
}