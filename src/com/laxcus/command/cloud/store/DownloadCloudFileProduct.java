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
 * 下载文件的处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/29/2021
 * @since laxcus 1.0
 */
public class DownloadCloudFileProduct extends EchoProduct {

	private static final long serialVersionUID = -2036016239270832678L;

	/** 存储资源定义器 **/
	private SRL srl;
	
	/** 状态 **/
	private int state;

	/**
	 * 构造下载文件的处理结果
	 */
	public DownloadCloudFileProduct() {
		super();
		state = StoreState.NONE;
	}
	
	/**
	 * 下载文件的处理结果
	 * @param state 状态
	 */
	public DownloadCloudFileProduct(int state) {
		this();
		setState(state);
	}
	
	/**
	 * 生成下载文件的处理结果副本
	 * @param that
	 */
	private DownloadCloudFileProduct(DownloadCloudFileProduct that) {
		this();
		srl = that.srl;
		state = that.state;
	}
	
	/**
	 * 从可类化读取器中解析下载文件的处理结果
	 * @param that 可类化读取器
	 */
	public DownloadCloudFileProduct(ClassReader reader) {
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DownloadCloudFileProduct duplicate() {
		return new DownloadCloudFileProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(srl);
		writer.writeInt(state);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		srl = reader.readInstance(SRL.class);
		state = reader.readInt();
	}

}