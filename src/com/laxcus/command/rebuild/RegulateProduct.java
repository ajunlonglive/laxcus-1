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
import com.laxcus.util.classable.*;

/**
 * 数据优化报告。
 * 
 * @author scott.liang
 * @version 1.1 6/2/2015
 * @since laxcus 1.0
 */
public class RegulateProduct extends EchoProduct {

	private static final long serialVersionUID = -2304258324319232843L;

	/** 列空间 **/
	private Dock dock;

	/** 优化后的数据块编号统计数 **/
	private int count;

	/**
	 * 根据传入实例，生成它的数据副本
	 * @param that RegulateProduct数据副本
	 */
	private RegulateProduct(RegulateProduct that) {
		super(that);
		dock = that.dock;
		count = that.count;
	}

	/**
	 * 构造默认的数据优化报告
	 */
	public RegulateProduct() {
		super();
		count = 0;
	}
	
	/**
	 * 构造数据优化报告，指定列空间
	 * 
	 * @param dock 列空间
	 */
	public RegulateProduct(Dock dock) {
		this();
		setDock(dock);
	}

	/**
	 * 构造数据优化报告，指定列空间和优化后的数据块编号统计数
	 * 
	 * @param dock 列空间
	 * @param count 优化后的数据块编号统计数
	 */
	public RegulateProduct(Dock dock, int count) {
		this(dock);
		setCount(count);
	}

	/**
	 * 从可类化数据读取器中解析数据优化报告 
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RegulateProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置列空间
	 * @param e Dock实例
	 */
	public void setDock(Dock e) {
		dock = e;
	}

	/**
	 * 返回列空间
	 * @return Dock实例
	 */
	public Dock getDock() {
		return dock;
	}

	/**
	 * 设置数据编号统计数
	 * @param i 数据编号统计数
	 */
	public void setCount(int i) {
		count = i;
	}

	/**
	 * 返回数据块编号总数
	 * @return 数据编号统计数
	 */
	public int getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public RegulateProduct duplicate() {
		return new RegulateProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(dock);
		writer.writeInt(count);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		dock = new Dock(reader);
		count = reader.readInt();
	}

}