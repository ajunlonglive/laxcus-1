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
import com.laxcus.util.net.*;

/**
 * 检测边缘服务监听处理结果
 * 
 * @author scott.liang
 * @version 1.0 10/20/2020
 * @since laxcus 1.0
 */
public class CheckTubListenerProduct extends TubProduct {

	/** 监听地址实例 **/
	private ArrayList<SocketHost> array = new ArrayList<SocketHost>();

	/**
	 * 构造默认的检测边缘服务监听处理结果
	 */
	public CheckTubListenerProduct() {
		super();
	}

	/**
	 * 生成检测边缘服务监听处理结果的副本
	 * @param that
	 */
	private CheckTubListenerProduct(CheckTubListenerProduct that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public CheckTubListenerProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存实例
	 * @param e
	 */
	public void add(SocketHost e) {
		Laxkit.nullabled(e);
		array.add(e);
	}

	/**
	 * 保存一批实例
	 * @param a
	 */
	public void addAll(Collection<SocketHost> a) {
		array.addAll(a);
	}

	/**
	 * 生成实例
	 * @return
	 */
	public List<SocketHost> list() {
		return new ArrayList<SocketHost>(array);
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
	public CheckTubListenerProduct duplicate() {
		return new CheckTubListenerProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.product.TubProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (SocketHost e : array) {
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
			SocketHost e = new SocketHost(reader);
			add(e);
		}
	}

}