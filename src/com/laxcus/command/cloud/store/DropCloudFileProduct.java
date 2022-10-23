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
 * 删除云端文件的处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/27/2021
 * @since laxcus 1.0
 */
public class DropCloudFileProduct extends EchoProduct {

	private static final long serialVersionUID = -6675469829298788298L;

	/** 云端文件 **/
	private VPath path;

	/** 状态 **/
	private int state;

	/**
	 * 构造删除云端文件的处理结果
	 */
	public DropCloudFileProduct() {
		super();
		state = StoreState.NONE;
	}
	
	/**
	 * 生成删除云端文件的处理结果副本
	 * @param that
	 */
	private DropCloudFileProduct(DropCloudFileProduct that) {
		this();
		path = that.path;
		state = that.state;
	}
	
	/**
	 * 从可类化读取器中解析删除云端文件的处理结果
	 * @param that 可类化读取器
	 */
	public DropCloudFileProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置云端文件
	 * @param e
	 */
	public void setPath(VPath e) {
		path = e;
	}

	/**
	 * 返回云端文件
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
	public DropCloudFileProduct duplicate() {
		return new DropCloudFileProduct(this);
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