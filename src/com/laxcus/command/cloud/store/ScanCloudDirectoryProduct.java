/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.store;

import com.laxcus.util.classable.*;

/**
 * 扫描云端磁盘结果
 * 
 * @author scott.liang
 * @version 1.0 10/30/2021
 * @since laxcus 1.0
 */
public class ScanCloudDirectoryProduct extends CloudProduct {

	private static final long serialVersionUID = -1969036104183725900L;

	/** 虚拟路径 **/
	private VPath path;

	/**
	 * 构造默认的扫描云端磁盘结果
	 */
	public ScanCloudDirectoryProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析扫描云端磁盘结果
	 * @param reader 可类化读取器
	 */
	public ScanCloudDirectoryProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成扫描云端磁盘结果副本
	 * @param that
	 */
	private ScanCloudDirectoryProduct(ScanCloudDirectoryProduct that) {
		super(that);
		path = that.path;
	}

	/**
	 * 设置虚拟路径
	 * @param e
	 */
	public void setVPath(VPath e) {
		path = e;
	}

	/**
	 * 返回虚拟路径
	 * @return
	 */
	public VPath getVPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ScanCloudDirectoryProduct duplicate() {
		return new ScanCloudDirectoryProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInstance(path);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		path = reader.readInstance(VPath.class);
	}

}