/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * TOP/HOME定时扫描用户关联的间隔时间。
 * 命令从WATCH站点发出。
 * 
 * @author scott.liang
 * @version 1.0 6/3/2018
 * @since laxcus 1.0
 */
public final class ScanLinkTimeProduct extends EchoProduct {

	private static final long serialVersionUID = -6857446861693487330L;

	/** 超时间隔时间 **/
	private long interval;

	/**
	 * 构造TOP/HOME定时扫描用户关联的间隔时间
	 */
	public ScanLinkTimeProduct() {
		super();
	}
	
	/**
	 * 从可类化数据读取中解析TOP/HOME定时扫描用户关联的间隔时间
	 * @param reader 可类化数据读取器
	 */
	public ScanLinkTimeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成命令副本
	 * @param that ScanLinkInterval实例
	 */
	private ScanLinkTimeProduct(ScanLinkTimeProduct that) {
		super(that);
		interval = that.interval;
	}

	/**
	 * 设置超时间隔时间
	 * @param ms 超时间隔时间
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回超时间隔时间
	 * @return 超时间隔时间
	 */
	public long getInterval() {
		return interval;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ScanLinkTimeProduct duplicate() {
		return new ScanLinkTimeProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(interval);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		interval = reader.readLong();
	}

}