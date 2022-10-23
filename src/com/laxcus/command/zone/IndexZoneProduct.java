/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.zone;

import java.util.*;

import com.laxcus.access.index.zone.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查找索引分布区域的处理结果。
 * 
 * @author scott.liang
 * @version 1.1 11/26/2015
 * @since laxcus 1.0
 */
public class IndexZoneProduct extends EchoProduct {
	
	private static final long serialVersionUID = -9144606932186962300L;
	
	/** 列索引区列表 **/
	private ArrayList<IndexZone> array = new ArrayList<IndexZone>();

	/**
	 * 构造默认的查找索引分布区域的处理结果
	 */
	public IndexZoneProduct() {
		super();
	}

	/**
	 * 根据传入的查找索引分布区域的处理结果，生成它的数据副本
	 * @param that IndexZoneProduct实例
	 */
	private IndexZoneProduct(IndexZoneProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/***
	 * 从可类化数据读取器中解析查找索引分布区域的处理结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public IndexZoneProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存索引分布区域
	 * @param e IndexZone实例
	 */
	public void add(IndexZone e) {
		Laxkit.nullabled(e);

		array.add(e);
	}

	/**
	 * 输出全部索引分布区域
	 * @return 返回IndexZone列表
	 */
	public List<IndexZone> list() {
		return new ArrayList<IndexZone>(array);
	}

	/**
	 * 统计查找的索引分布区域的尺寸
	 * @return 返回索引分布区域的尺寸数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断查找索引分布区域集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public IndexZoneProduct duplicate() {
		return new IndexZoneProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (IndexZone e : array) {
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
			IndexZone e = IndexZoneCreator.resolve(reader);
			array.add(e);
		}
	}

}