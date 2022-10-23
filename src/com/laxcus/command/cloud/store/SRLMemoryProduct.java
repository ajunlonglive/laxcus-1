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
 * @version 1.0 11/07/2021
 * @since laxcus 1.0
 */
public class SRLMemoryProduct extends EchoProduct {

	private static final long serialVersionUID = -7941966456539157561L;

	/** 存储资源定义器 **/
	private SRL srl;

	/** 数据流 **/
	private byte[] data;

	/**
	 * 构造下载文件的处理结果
	 */
	public SRLMemoryProduct() {
		super();
	}

	/**
	 * 构造下载文件的处理结果
	 * @param srl SRL
	 * @param b 内存数据
	 */
	public SRLMemoryProduct(SRL srl, byte[] b) {
		this();
		setSRL(srl);
		setData(b);
	}

	/**
	 * 生成下载文件的处理结果副本
	 * @param that
	 */
	private SRLMemoryProduct(SRLMemoryProduct that) {
		this();
		srl = that.srl;
		data = that.data;
	}

	/**
	 * 从可类化读取器中解析下载文件的处理结果
	 * @param that 可类化读取器
	 */
	public SRLMemoryProduct(ClassReader reader) {
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
	 * 设置内存数据
	 * @param b
	 */
	public void setData(byte[] b) {
		data = b;
	}

	/**
	 * 返回内存数据
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SRLMemoryProduct duplicate() {
		return new SRLMemoryProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(srl);
		writer.writeByteArray(data);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		srl = reader.readInstance(SRL.class);
		data = reader.readByteArray();
	}

}