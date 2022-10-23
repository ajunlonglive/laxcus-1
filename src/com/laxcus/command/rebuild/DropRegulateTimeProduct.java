/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 撤销数据优化时间处理结果
 * 
 * @author scott.liang
 * @version 1.1 12/01/2015
 * @since laxcus 1.0
 */
public final class DropRegulateTimeProduct extends ConfirmProduct {
	
	private static final long serialVersionUID = 3446877934759048921L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 构造默认的撤销数据优化时间处理结果
	 */
	private DropRegulateTimeProduct() {
		super();
	}

	/**
	 * 构造撤销数据优化时间处理结果，指定成功标识
	 * @param space 数据表名
	 * @param successful 成功标识
	 */
	public DropRegulateTimeProduct(Space space, boolean successful) {
		this();
		setSpace(space);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析撤销数据优化时间处理结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DropRegulateTimeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成撤销数据优化时间处理结果数据副本
	 * @param that DropRegulateTimeProduct实例
	 */
	private DropRegulateTimeProduct(DropRegulateTimeProduct that) {
		super(that);
		space = that.space;
	}

	/**
	 * 设置数据表名
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);
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
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DropRegulateTimeProduct duplicate() {
		return new DropRegulateTimeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		space = new Space(reader);
	}

}