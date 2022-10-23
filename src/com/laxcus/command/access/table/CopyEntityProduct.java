/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 复制数据块报告
 * 
 * @author scott.liang
 * @version 1.0 11/10/2020
 * @since laxcus 1.0
 */
public class CopyEntityProduct extends EchoProduct {

	private static final long serialVersionUID = 7460986076468193115L;

	/** 单元数组 **/
	private ArrayList<CopyEntityItem> array = new ArrayList<CopyEntityItem>();

	/**
	 * 构造默认的复制数据块报告
	 */
	public CopyEntityProduct() {
		super();
	}

	/**
	 * 构造复制数据块报告，指定单元
	 * @param item 单元
	 */
	public CopyEntityProduct(CopyEntityItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器解析复制数据块报告
	 * @param reader 可类化数据读取器
	 */
	public CopyEntityProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造复制数据块报告的数据副本
	 * @param that 复制数据块报告
	 */
	private CopyEntityProduct(CopyEntityProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理单元，不允许空指针
	 * @param e CopyEntityItem实例
	 * @return 返回真或者假
	 */
	public boolean add(CopyEntityItem e) {
		Laxkit.nullabled(e);
		// 判断存在
		if (array.contains(e)) {
			return false;
		}
		return array.add(e);
	}
	
	/**
	 * 保存一个处理单元
	 * @param stub 数据块编号
	 * @param successful 成功标记
	 * @return 返回真或者假
	 */
	public boolean add(long stub, boolean successful) {
		return add(new CopyEntityItem(stub, successful));
	}

	/**
	 * 保存一批处理单元
	 * @param a CopyEntityItem列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<CopyEntityItem> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (CopyEntityItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理单元
	 * @param e CopyEntityProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(CopyEntityProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个处理单元
	 * @param e CopyEntityItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(CopyEntityItem e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e CopyEntityItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(CopyEntityItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部处理单元列表
	 * @return CopyEntityItem列表
	 */
	public List<CopyEntityItem> list() {
		return new ArrayList<CopyEntityItem>(array);
	}

	/**
	 * 统计处理单元成员数目
	 * @return 处理单元成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CopyEntityProduct duplicate() {
		return new CopyEntityProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (CopyEntityItem e : array) {
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
			CopyEntityItem item = new CopyEntityItem(reader);
			array.add(item);
		}
	}

}