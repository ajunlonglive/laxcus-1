/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 删除目录的处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/27/2021
 * @since laxcus 1.0
 */
public class DropCloudDirectoryProduct extends EchoProduct {

	private static final long serialVersionUID = -4603914090666726605L;

	/** 存储目录 **/
	private VPath path;
	
	/** 状态 **/
	private int state;

	/**
	 * 构造删除目录的处理结果
	 */
	public DropCloudDirectoryProduct() {
		super();
		state = StoreState.NONE;
	}
	
	/**
	 * 生成删除目录的处理结果副本
	 * @param that
	 */
	private DropCloudDirectoryProduct(DropCloudDirectoryProduct that) {
		this();
		path = that.path;
		state = that.state;
	}
	
	/**
	 * 从可类化读取器中解析删除目录的处理结果
	 * @param that 可类化读取器
	 */
	public DropCloudDirectoryProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置目录
	 * @param e
	 */
	public void setPath(VPath e) {
		path = e;
	}

	/**
	 * 返回目录
	 * @return
	 */
	public VPath getPath() {
		return path;
	}

	/**
	 * 设置处理状态
	 * @param who
	 */
	public void setState(int who) {
		state = who;
	}

	/**
	 * 返回处理状态
	 * @return
	 */
	public int getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DropCloudDirectoryProduct duplicate() {
		return new DropCloudDirectoryProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(path);
		writer.writeInt(state);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		path = reader.readInstance(VPath.class);
		state = reader.readInt();
	}

}