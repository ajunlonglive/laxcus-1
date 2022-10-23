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
 * 生成目录的处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/23/2021
 * @since laxcus 1.0
 */
public class CreateCloudDirectoryProduct extends EchoProduct {

	private static final long serialVersionUID = 1664651729012155472L;

	/** 状态 **/
	private int state;
	
	/** 生成虚路径 **/
	private VPath path;

	/**
	 * 构造生成目录的处理结果
	 */
	public CreateCloudDirectoryProduct() {
		super();
		state = StoreState.NONE;
	}
	
	/**
	 * 生成生成目录的处理结果副本
	 * @param that
	 */
	private CreateCloudDirectoryProduct(CreateCloudDirectoryProduct that) {
		this();
		state = that.state;
		path = that.path;
	}
	
	/**
	 * 从可类化读取器中解析生成目录的处理结果
	 * @param that 可类化读取器
	 */
	public CreateCloudDirectoryProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

//	/**
//	 * 设置目录
//	 * @param e
//	 */
//	public void setDirectory(SDirectory e) {
//		dir = e;
//	}
//
//	/**
//	 * 返回目录
//	 * @return
//	 */
//	public SDirectory getDirectory() {
//		return dir;
//	}
	
	/**
	 * 生成虚路径
	 * @param v
	 */
	public void setPath(VPath v) {
		path = v;
	}

	/**
	 * 返回虚路径
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
	public CreateCloudDirectoryProduct duplicate() {
		return new CreateCloudDirectoryProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(state);
		writer.writeInstance(path);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		state = reader.readInt();
		path = reader.readInstance(VPath.class);
	}

}