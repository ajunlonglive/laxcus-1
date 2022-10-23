/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.fast;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块快速处理报告。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class FastMassProduct extends EchoProduct {
	
	private static final long serialVersionUID = -4938154013239118020L;

	/** 处理结果单元数组 **/
	private ArrayList<FastMassItem> array = new ArrayList<FastMassItem>();

	/**
	 * 从传入的数据块快速处理报告，生成它的数据副本
	 * @param that FastMassProduct实例
	 */
	private FastMassProduct(FastMassProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造数据块快速处理报告
	 */
	public FastMassProduct() {
		super();
	}
	
	/**
	 * 构造数据块快速处理报告，指定数据块快速处理单元
	 * @param item FastMassItem实例
	 */
	public FastMassProduct(FastMassItem item) {
		this();
		add(item);
	}
	
	/**
	 * 从可类化数据读取器中解析数据块快速处理报告参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FastMassProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 增加一个数据块快速处理单元，不允许空指针
	 * @param e FastMassItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(FastMassItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 保存一批数据块快速处理单元数组
	 * @param a FastMassItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<FastMassItem> a) {
		int size = array.size();
		for (FastMassItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存另一组数据块快速处理报告
	 * @param e FastMassProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(FastMassProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部数据块快速处理单元
	 * @return FastMassItem列表
	 */
	public List<FastMassItem> list() {
		return array;
	}
	
	/**
	 * 统计数据块快速处理单元数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public FastMassProduct duplicate() {
		return new FastMassProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for(FastMassItem node: array) {
			writer.writeObject(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FastMassItem node = new FastMassItem(reader);
			array.add(node);
		}
	}

}
