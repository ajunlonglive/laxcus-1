/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 上传文件的处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/29/2021
 * @since laxcus 1.0
 */
public class UploadCloudFileProduct extends EchoProduct {

	private static final long serialVersionUID = -2036016239270832678L;

	/** 存储资源定义器 **/
	private SRL srl;
	
	/** 状态 **/
	private int state;
	
	/** 生成虚路径 **/
	private VPath path;

	/**
	 * 构造上传文件的处理结果
	 */
	public UploadCloudFileProduct() {
		super();
		state = StoreState.NONE;
	}
	
	/**
	 * 上传文件的处理结果
	 * @param state 状态
	 */
	public UploadCloudFileProduct(int state) {
		this();
		setState(state);
	}
	
	/**
	 * 生成上传文件的处理结果副本
	 * @param that
	 */
	private UploadCloudFileProduct(UploadCloudFileProduct that) {
		this();
		srl = that.srl;
		state = that.state;
		path = that.path;
	}
	
	/**
	 * 从可类化读取器中解析上传文件的处理结果
	 * @param that 可类化读取器
	 */
	public UploadCloudFileProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置资源定义器
	 * @param e
	 */
	public void setSRL(SRL e) {
		srl = e;
	}

	/**
	 * 返回资源定义器
	 * @return
	 */
	public SRL getSRL() {
		return srl;
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public UploadCloudFileProduct duplicate() {
		return new UploadCloudFileProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(srl);
		writer.writeInt(state);
		writer.writeInstance(path);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		srl = reader.readInstance(SRL.class);
		state = reader.readInt();
		path = reader.readInstance(VPath.class);
	}

}