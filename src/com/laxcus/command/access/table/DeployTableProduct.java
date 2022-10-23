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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 部署数据表参数结果
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class DeployTableProduct extends EchoProduct {
	
	private static final long serialVersionUID = -6015497472211412518L;

	/** 被部署的处理单元集合 **/
	private TreeSet<DeployTableItem> array = new TreeSet<DeployTableItem>();

	/**
	 * 构造默认的部署数据表参数结果
	 */
	public DeployTableProduct() {
		super();
	}

	/**
	 * 构造部署数据表参数结果，保存一个单元
	 * @param item 单元
	 */
	public DeployTableProduct(DeployTableItem item) {
		this();
		add(item);
	}
	
	/**
	 * 从可类化数据读取器中解析部署数据表参数结果
	 * @param reader 可类化数据读取器
	 */
	public DeployTableProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的部署数据表参数结果，生成它的数据副本
	 * @param that DeployTableProduct实例
	 */
	private DeployTableProduct(DeployTableProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果
	 * @param e DeployTableItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(DeployTableItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理结果
	 * @param seat 数据表基点
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Seat seat, boolean successful) {
		return add(new DeployTableItem(seat, successful));
	}

	/**
	 * 保存一个处理结果
	 * @param seat 数据表基点
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Siger siger, Node node, boolean successful) {
		Seat seat = new Seat(siger, node);
		return add(new DeployTableItem(seat, successful));
	}
	
	/**
	 * 保存一批处理结果
	 * @param a DeployTableItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<DeployTableItem> a) {
		int size = array.size();
		for (DeployTableItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批处理结果
	 * @param e DeployTableProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(DeployTableProduct e) {
		return addAll(e.array);
	}
	
	/**
	 * 输出全部关联的处理结果
	 * @param siger 数据表签名
	 * @return 返回关联结果
	 */
	public List<DeployTableItem> find(Siger siger) {
		ArrayList<DeployTableItem> a = new ArrayList<DeployTableItem>();
		for (DeployTableItem e : array) {
			if (Laxkit.compareTo(e.getSiger(), siger) == 0) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 输出全部被部署的处理单元
	 * @return 返回DeployTableItem列表
	 */
	public List<DeployTableItem> list() {
		return new ArrayList<DeployTableItem>(array);
	}

	/**
	 * 统计成员数目
	 * @return 返回被部署的处理单元成员数目
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

	/**
	 * 清除全部
	 */
	public void clear() {
		array.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (DeployTableItem e : array) {
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
			DeployTableItem e = new DeployTableItem(reader);
			array.add(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DeployTableProduct duplicate() {
		return new DeployTableProduct(this);
	}

}