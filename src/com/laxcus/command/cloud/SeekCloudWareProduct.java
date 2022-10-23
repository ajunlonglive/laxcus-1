/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检索云应用处理结果
 * 
 * @author scott.liang
 * @version 1.0 2/9/2020
 * @since laxcus 1.0
 */
public class SeekCloudWareProduct extends EchoProduct {

	private static final long serialVersionUID = -2554858307640868801L;

	/** 授权单元集合 **/
	private TreeSet<CloudWareItem> array = new TreeSet<CloudWareItem>();

	/**
	 * 构造默认的检索云应用处理结果
	 */
	public SeekCloudWareProduct() {
		super();
	}
	
	/**
	 * 检索云应用处理结果，指定一个单元
	 * @param item 单元
	 */
	public SeekCloudWareProduct(CloudWareItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器中解析共享资源处理结果
	 * @param reader 可类化数据读取器
	 */
	public SeekCloudWareProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成检索云应用处理结果的数据副本
	 * @param that SeekCloudWareProduct实例
	 */
	private SeekCloudWareProduct(SeekCloudWareProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存授权单元
	 * @param e CloudWareItem实例
	 * @return 返回真或者假
	 */
	public boolean add(CloudWareItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}


	/**
	 * 保存一批单元
	 * @param a CloudWareItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<CloudWareItem> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 保存一批单元
	 * @param a SeekCloudWareProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SeekCloudWareProduct a) {
		return addAll(a.array);
	}

	/**
	 * 输出单元数组
	 * @return 返回CloudWareItem列表
	 */
	public List<CloudWareItem> list() {
		return new ArrayList<CloudWareItem>(array);
	}

	/**
	 * 统计成员数
	 * @return 返回成员数目
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
	public SeekCloudWareProduct duplicate() {
		return new SeekCloudWareProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (CloudWareItem e : array) {
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
			CloudWareItem e = new CloudWareItem(reader);
			array.add(e);
		}
	}

}