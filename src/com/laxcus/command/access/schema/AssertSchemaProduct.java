/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 判断数据库存在报告
 * 
 * @author scott.liang
 * @version 1.1 8/1/2015
 * @since laxcus 1.0
 */
public class AssertSchemaProduct extends ConfirmProduct {

	private static final long serialVersionUID = -577621394592061188L;
	
	/** 数据库名 **/
	private Fame fame;
	
	/**
	 * 构造默认的判断数据库存在报告
	 */
	private AssertSchemaProduct() {
		super();
	}

	/**
	 * 生成一个判断数据库存在报告数据副本
	 * @param that AssertSchemaProduct实例
	 */
	private AssertSchemaProduct(AssertSchemaProduct that) {
		super(that);
		fame = that.fame;
	}

	/**
	 * 建立判断用户账号报告，指定数据库名和成功标识
	 * @param fame 数据库名
	 * @param successful 成功标识
	 */
	public AssertSchemaProduct(Fame fame, boolean successful) {
		this();
		setFame(fame);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析判断数据库存在报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public AssertSchemaProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据库名
	 * @param e Fame实例
	 */
	public void setFame(Fame e) {
		fame = e;
	}

	/**
	 * 返回数据库名
	 * @return Fame实例
	 */
	public Fame getFame() {
		return fame;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(fame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		fame = reader.readInstance(Fame.class);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public AssertSchemaProduct duplicate() {
		return new AssertSchemaProduct(this);
	}
}