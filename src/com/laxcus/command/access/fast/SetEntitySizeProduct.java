/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.fast;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 设置数据块尺寸处理结果
 * 
 * @author scott.liang
 * @version 1.1 12/01/2015
 * @since laxcus 1.0
 */
public final class SetEntitySizeProduct extends ConfirmProduct {
	
	private static final long serialVersionUID = 5785944480284125156L;

	/**
	 * 构造默认的设置数据块尺寸处理结果
	 */
	public SetEntitySizeProduct() {
		super();
	}

	/**
	 * 构造设置数据块尺寸处理结果，指定成功标识
	 * @param b 成功标识
	 */
	public SetEntitySizeProduct(boolean b) {
		this();
		setSuccessful(b);
	}

	/**
	 * 从可类化数据读取器中解析设置数据块尺寸处理结果
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public SetEntitySizeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成设置数据块尺寸处理结果数据副本
	 * @param that SetEntitySizeProduct实例
	 */
	private SetEntitySizeProduct(SetEntitySizeProduct that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SetEntitySizeProduct duplicate() {
		return new SetEntitySizeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

}