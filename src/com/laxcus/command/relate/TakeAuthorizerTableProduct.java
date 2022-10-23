/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 授权人数据表报告
 * 
 * @author scott.liang
 * @version 1.0 8/7/2018
 * @since laxcus 1.0
 */
public class TakeAuthorizerTableProduct extends EchoProduct {

	private static final long serialVersionUID = -4906217393457618337L;

	/** 授权人数据表 **/
	private TreeSet<Table> array = new TreeSet<Table>();

	/**
	 * 构造默认的授权人数据表报告
	 */
	public TakeAuthorizerTableProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析授权人数据表报告
	 * @param reader 可类化数据读取器
	 */
	public TakeAuthorizerTableProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成授权人数据表报告的数据副本
	 * @param that 授权人数据表报告实例
	 */
	private TakeAuthorizerTableProduct(TakeAuthorizerTableProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个数据表
	 * @param e 数据表实例
	 * @return 返回真或者假
	 */
	public boolean add(Table e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批数据表
	 * @param a 数据表数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Table> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 保存一批数据表
	 * @param a 授权人数据表报告
	 * @return 返回新增成员数目
	 */
	public int addAll(TakeAuthorizerTableProduct a) {
		return addAll(a.array);
	}

	/**
	 * 输出全部数据表
	 * @return 数据表列表
	 */
	public List<Table> list() {
		return new ArrayList<Table>(array);
	}

	/**
	 * 统计成员数
	 * @return 数据表数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return  返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeAuthorizerTableProduct duplicate() {
		return new TakeAuthorizerTableProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Table e : array) {
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
			Table e = new Table(reader);
			array.add(e);
		}
	}

}