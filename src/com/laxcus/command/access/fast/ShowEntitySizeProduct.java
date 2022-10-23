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
 * 获数据块尺寸结果
 * 
 * @author scott.liang
 * @version 1.0 12/15/2017
 * @since laxcus 1.0
 */
public class ShowEntitySizeProduct extends EchoProduct {

	private static final long serialVersionUID = -8224177218618183099L;

	/** 数据块尺寸 **/
	private int length;

	/**
	 * 构造默认和私有获数据块尺寸结果
	 */
	private ShowEntitySizeProduct() {
		super();
	}

	/**
	 * 生成获数据块尺寸结果的数据副本
	 * @param that 获数据块尺寸结果
	 */
	private ShowEntitySizeProduct(ShowEntitySizeProduct that) {
		super(that);
		length = that.length;
	}
	
	/**
	 * 构造获数据块尺寸结果，指定参数
	 * @param length 获数据块尺寸
	 */
	public ShowEntitySizeProduct(int length) {
		this();
		setLength(length);
	}

	/**
	 * 从可类化数据读取器中解析获数据块尺寸结果
	 * @param reader 可类化数据读取器
	 */
	public ShowEntitySizeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据块长度
	 * @param i
	 */
	public void setLength(int i) {
		length = i;
	}

	/**
	 * 返回数据块长度
	 * @return
	 */
	public int getLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ShowEntitySizeProduct duplicate() {
		return new ShowEntitySizeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(length);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		length = reader.readInt();
	}

}