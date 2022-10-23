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
 * 撤销用户参数结果
 * 
 * @author scott.liang
 * @version 1.0 6/1/2015
 * @since laxcus 1.0
 */
public class EraseUserProduct extends EchoProduct {
	
	private static final long serialVersionUID = -7383265713010677490L;

	/** 被发布的处理单元集合 **/
	private TreeSet<EraseUserItem> array = new TreeSet<EraseUserItem>();

	/**
	 * 构造默认的撤销用户参数结果
	 */
	public EraseUserProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析撤销用户参数结果
	 * @param reader 可类化数据读取器
	 */
	public EraseUserProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的撤销用户参数结果，生成它的数据副本
	 * @param that CancelUserProduct实例
	 */
	private EraseUserProduct(EraseUserProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果
	 * @param e CancelUserItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(EraseUserItem e) {
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
		return add(new EraseUserItem(seat, successful));
	}

	/**
	 * 保存一个处理结果
	 * @param seat 用户基点
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Siger siger, Node node, boolean successful) {
		Seat seat = new Seat(siger, node);
		return add(new EraseUserItem(seat, successful));
	}
	
	/**
	 * 保存一批处理结果
	 * @param a CancelUserItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<EraseUserItem> a) {
		int size = array.size();
		for (EraseUserItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批处理结果
	 * @param e CancelUserProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(EraseUserProduct e) {
		return addAll(e.array);
	}
	
	/**
	 * 输出全部关联的处理结果
	 * @param siger 用户签名
	 * @return 返回关联结果
	 */
	public List<EraseUserItem> find(Siger siger) {
		ArrayList<EraseUserItem> a = new ArrayList<EraseUserItem>();
		for (EraseUserItem e : array) {
			if (Laxkit.compareTo(e.getSiger(), siger) == 0) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 输出全部被发布的处理单元
	 * @return 返回CancelUserItem列表
	 */
	public List<EraseUserItem> list() {
		return new ArrayList<EraseUserItem>(array);
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
		for (EraseUserItem e : array) {
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
			EraseUserItem e = new EraseUserItem(reader);
			array.add(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public EraseUserProduct duplicate() {
		return new EraseUserProduct(this);
	}

}