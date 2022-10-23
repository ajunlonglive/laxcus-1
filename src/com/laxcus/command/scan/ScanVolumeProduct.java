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
 * 数据卷扫描报告
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class ScanVolumeProduct extends EchoProduct {
	
	private static final long serialVersionUID = 2789924707791198493L;

	/** 扫描结果 **/
	private TreeSet<ScanVolumeItem> array = new TreeSet<ScanVolumeItem>();
	
	/**
	 * 建立数据卷扫描报告的数据副本
	 * @param that 数据卷扫描报告实例
	 */
	private ScanVolumeProduct(ScanVolumeProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造一个默认的数据卷扫描报告
	 */
	public ScanVolumeProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析数据卷扫描报告
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanVolumeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 累积数据卷扫描单元
	 * @param item 数据卷扫描单元
	 */
	public void cumulate(ScanVolumeItem item) {
		Laxkit.nullabled(item);

		// 找到匹配的表名，合并数据
		for (ScanVolumeItem e : array) {
			// 保存成功，退出
			if (e.cumulate(item)) {
				return;
			}
		}
		// 以上不成立，保存它
		array.add(item);
	}

	/**
	 * 累积数据卷扫描单元
	 * @param product 数据表扫描结果
	 */
	public void cumulate(ScanTableProduct product) {
		for (ScanTableItem item : product.list()) {
			ScanVolumeItem e = item.createVolume();
			cumulate(e);
		}
	}
	
	/**
	 * 保存一个数据表扫描单元
	 * @param e ScanVolumeItem实例
	 */
	public void add(ScanVolumeItem e) {
		Laxkit.nullabled(e);

		cumulate(e);
	}

	/**
	 * 保存一批数据表扫描单元
	 * @param a ScanVolumeItem数组
	 */
	public void addAll(Collection<ScanVolumeItem> a) {
		for(ScanVolumeItem e : a) {
			add(e);
		}
	}
	
	/**
	 * 保存一批数据表扫描单元
	 * @param e 数据卷扫描报告实例
	 */
	public void addAll(ScanVolumeProduct e) {
		addAll(e.array);
	}

	/**
	 * 输出全部单元列表
	 * @return ScanVolumeItem列表
	 */
	public List<ScanVolumeItem> list() {
		return new ArrayList<ScanVolumeItem>(array);
	}

	/**
	 * 统计单元数目
	 * @return ScanVolumeItem单元数目
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
	public ScanVolumeProduct duplicate() {
		return new ScanVolumeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ScanVolumeItem e : array) {
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
			ScanVolumeItem e = new ScanVolumeItem(reader);
			array.add(e);
		}
	}

}