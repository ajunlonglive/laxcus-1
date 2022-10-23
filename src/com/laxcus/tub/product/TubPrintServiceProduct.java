/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.product;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 运行边缘服务处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/20/2020
 * @since laxcus 1.0
 */
public class TubPrintServiceProduct extends TubProduct {

	/** 监听地址实例 **/
	private ArrayList<TubServiceItem> array = new ArrayList<TubServiceItem>();

	/**
	 * 构造默认的运行边缘服务处理结果
	 */
	public TubPrintServiceProduct() {
		super();
	}

	/**
	 * 生成运行边缘服务处理结果的副本
	 * @param that
	 */
	private TubPrintServiceProduct(TubPrintServiceProduct that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public TubPrintServiceProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存实例
	 * @param e
	 */
	public void add(TubServiceItem e) {
		Laxkit.nullabled(e);
		array.add(e);
	}

	/**
	 * 保存一批实例
	 * @param a
	 */
	public void addAll(Collection<TubServiceItem> a) {
		array.addAll(a);
	}

	/**
	 * 生成实例
	 * @return
	 */
	public List<TubServiceItem> list() {
		return new ArrayList<TubServiceItem>(array);
	}

	/**
	 * 返回成员数
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空值
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.product.TubProduct#duplicate()
	 */
	@Override
	public TubPrintServiceProduct duplicate() {
		return new TubPrintServiceProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.product.TubProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (TubServiceItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.product.TubProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			TubServiceItem e = new TubServiceItem(reader);
			add(e);
		}
	}

}