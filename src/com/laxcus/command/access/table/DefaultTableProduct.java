/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 数据表处理结果
 * 
 * @author scott.liang
 * @version 1.0 6/21/2016
 * @since laxcus 1.0
 */
public abstract class DefaultTableProduct extends MultiProcessProduct {

	private static final long serialVersionUID = 8547124967745512393L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 构造默认的数据表处理结果
	 */
	protected DefaultTableProduct() {
		super();
	}

	/**
	 * 构造数据表处理结果，指定数据表名
	 * @param space 数据表名
	 */
	protected DefaultTableProduct(Space space) {
		this();
		setSpace(space);
	}
	
	/**
	 * 生成数据表处理结果的数据副本
	 * @param that DefaultTableProduct实例
	 */
	protected DefaultTableProduct(DefaultTableProduct that) {
		super(that);
		space = that.space;
	}
	
	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		space = reader.readInstance(Space.class);
	}

}
