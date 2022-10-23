/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * DeleteCacheReflex命令处理结果
 * 
 * @author scott.liang
 * @version 1.1 6/13/2015
 * @since laxcus 1.0
 */
public final class DeleteCacheReflexProduct extends ConfirmProduct {
	
	private static final long serialVersionUID = 8317300731212087734L;

	/** 数据块标识 **/
	private StubFlag flag;

	/**
	 * 构造默认的DeleteReflexCache命令处理结果
	 */
	private DeleteCacheReflexProduct() {
		super();
	}

	/**
	 * 建立DeleteReflexCache命令处理结果的数据副本
	 * @param that DeleteCacheReflexProduct实例
	 */
	private DeleteCacheReflexProduct(DeleteCacheReflexProduct that) {
		super(that);
		flag = that.flag;
	}
	
	/**
	 * 构造DeleteReflexCache命令处理结果，指定参数
	 * @param flag 数据块标识
	 * @param success 删除成功
	 */
	public DeleteCacheReflexProduct(StubFlag flag, boolean success) {
		this();
		setFlag(flag);
		setSuccessful(success);
	}

	/**
	 * 构造DeleteReflexCache命令处理结果，指定参数
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param success 成功标识
	 */
	public DeleteCacheReflexProduct(Space space, long stub, boolean success) {
		this(new StubFlag(space, stub), success);
	}

	/**
	 * 从可类化数据读取器中解析DeleteReflexCache命令处理结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DeleteCacheReflexProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据块标识
	 * @param e StubFlag实例
	 */
	public void setFlag(StubFlag e) {
		Laxkit.nullabled(e);

		flag = e;
	}
	
	/**
	 * 返回数据块标识
	 * @return StubFlag实例
	 */
	public StubFlag getFlag() {
		return flag;
	}
	
	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return flag.getSpace();
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return flag.getStub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DeleteCacheReflexProduct duplicate() {
		return new DeleteCacheReflexProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(flag);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		flag = new StubFlag(reader);
	}

}