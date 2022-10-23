/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 发布用户参数结果
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class DeployUserProduct extends EchoProduct {
	
	private static final long serialVersionUID = 2250222766177743240L;
	
	/** 被发布的处理单元集合 **/
	private TreeSet<DeployUserItem> array = new TreeSet<DeployUserItem>();

	/**
	 * 构造默认的发布用户参数结果
	 */
	public DeployUserProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析发布用户参数结果
	 * @param reader 可类化数据读取器
	 */
	public DeployUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的发布用户参数结果，生成它的数据副本
	 * @param that DeployUserProduct实例
	 */
	private DeployUserProduct(DeployUserProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果
	 * @param e DeployUserItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(DeployUserItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理结果
	 * @param seat 用户基点
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Seat seat, boolean successful) {
		return add(new DeployUserItem(seat, successful));
	}

	/**
	 * 保存一个处理结果
	 * @param seat 用户基点
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Siger siger, Node node, boolean successful) {
		Seat seat = new Seat(siger, node);
		return add(new DeployUserItem(seat, successful));
	}
	
	/**
	 * 保存一批处理结果
	 * @param a DeployUserItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<DeployUserItem> a) {
		int size = array.size();
		for (DeployUserItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批处理结果
	 * @param e DeployUserProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(DeployUserProduct e) {
		return addAll(e.array);
	}
	
	/**
	 * 输出全部关联的处理结果
	 * @param siger 用户签名
	 * @return 返回关联结果
	 */
	public List<DeployUserItem> find(Siger siger) {
		ArrayList<DeployUserItem> a = new ArrayList<DeployUserItem>();
		for (DeployUserItem e : array) {
			if (Laxkit.compareTo(e.getSiger(), siger) == 0) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 输出全部被发布的处理单元
	 * @return 返回DeployUserItem列表
	 */
	public List<DeployUserItem> list() {
		return new ArrayList<DeployUserItem>(array);
	}

	/**
	 * 统计成员数目
	 * @return 返回被发布的处理单元成员数目
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
		for (DeployUserItem e : array) {
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
			DeployUserItem e = new DeployUserItem(reader);
			array.add(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public DeployUserProduct duplicate() {
		return new DeployUserProduct(this);
	}

}