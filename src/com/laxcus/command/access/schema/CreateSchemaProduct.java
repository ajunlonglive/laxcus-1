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
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建立数据库处理结果
 * 
 * @author scott.liang
 * @version 1.1 6/26/2015
 * @since laxcus 1.0
 */
public final class CreateSchemaProduct extends ConfirmProduct {
	
	private static final long serialVersionUID = 4076093238704606591L;
	
	/** 数据库名称 **/
	private Fame fame;

	/**
	 * 构造默认和私有的建立数据库处理结果
	 */
	private CreateSchemaProduct() {
		super();
	}

	/**
	 * 生成一个建立数据库处理结果数据副本
	 * @param that
	 */
	public CreateSchemaProduct(CreateSchemaProduct that) {
		super(that);
		fame = that.fame;
	}

	/**
	 * 建立建立数据库处理结果，指定数据库名
	 * @param fame - 数据库名称
	 * @param successful - 成功标识
	 */
	public CreateSchemaProduct(Fame fame, boolean successful) {
		this();
		setFame(fame);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析建立数据库处理结果
	 * @param reader - 可类化数据读取器
	 * @since 1.1
	 */
	public CreateSchemaProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据库名称
	 * @param e Fame实例
	 */
	public void setFame(Fame e) {
		Laxkit.nullabled(e);

		fame = e;
	}

	/**
	 * 返回数据库名称
	 * @return Fame实例
	 */
	public Fame getFame() {
		return fame;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateSchemaProduct duplicate() {
		return new CreateSchemaProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(fame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		fame = new Fame(reader);
	}

}