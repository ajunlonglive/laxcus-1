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
 * 扫描数据块报告。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class ScanEntityProduct extends EchoProduct {
	
	private static final long serialVersionUID = 1795841698145119487L;

	/** 处理结果单元数组 **/
	private TreeSet<ScanEntityItem> array = new TreeSet<ScanEntityItem>();

	/**
	 * 从传入的扫描数据块报告，生成它的数据副本
	 * @param that ScanEntityProduct实例
	 */
	private ScanEntityProduct(ScanEntityProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造扫描数据块报告
	 */
	public ScanEntityProduct() {
		super();
	}
	
	/**
	 * 构造扫描数据块报告，指定扫描数据块单元
	 * @param item ScanEntityItem实例
	 */
	public ScanEntityProduct(ScanEntityItem item) {
		this();
		add(item);
	}
	
	/**
	 * 从可类化数据读取器中解析扫描数据块报告参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanEntityProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 增加一个扫描数据块单元，不允许空指针
	 * @param e ScanEntityItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ScanEntityItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 保存一批扫描数据块单元数组
	 * @param a ScanEntityItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<ScanEntityItem> a) {
		int size = array.size();
		for (ScanEntityItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存另一组扫描数据块报告
	 * @param e ScanEntityProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ScanEntityProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部扫描数据块单元
	 * @return ScanEntityItem列表
	 */
	public List<ScanEntityItem> list() {
		return new ArrayList<ScanEntityItem>(array);
	}
	
	/**
	 * 统计扫描数据块单元数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ScanEntityProduct duplicate() {
		return new ScanEntityProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for(ScanEntityItem item: array) {
			writer.writeObject(item);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ScanEntityItem item = new ScanEntityItem(reader);
			array.add(item);
		}
	}

}