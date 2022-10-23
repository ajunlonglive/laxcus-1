/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据表数据扫描报告
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class ScanTableProduct extends EchoProduct {
	
	private static final long serialVersionUID = -7315559059791037630L;

	/** 扫描数组 **/
	private TreeSet<ScanTableItem> array = new TreeSet<ScanTableItem>();
	
	/**
	 * 建立数据表数据扫描报告的数据副本
	 * @param that ScanTableProduct实例
	 */
	private ScanTableProduct(ScanTableProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造一个默认的数据表数据扫描报告
	 */
	public ScanTableProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析数据表数据扫描报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanTableProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个数据表扫描单元
	 * @param e ScanTableItem实例
	 */
	public void add(ScanTableItem e) {
		Laxkit.nullabled(e);

		array.add(e);
	}

	/**
	 * 保存一批数据表扫描单元
	 * @param a ScanTableItem数组
	 */
	public void addAll(Collection<ScanTableItem> a) {
		array.addAll(a);
	}
	
	/**
	 * 保存一批数据表扫描单元
	 * @param e ScanTableProduct实例
	 */
	public void addAll(ScanTableProduct e) {
		addAll(e.array);
	}

	/**
	 * 输出全部单元列表
	 * @return ScanTableItem列表
	 */
	public List<ScanTableItem> list() {
		return new ArrayList<ScanTableItem>(array);
	}

	/**
	 * 统计单元数目
	 * @return ScanTableItem单元数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ScanTableProduct duplicate() {
		return new ScanTableProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ScanTableItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ScanTableItem e = new ScanTableItem(reader);
			array.add(e);
		}
	}

}