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
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 多用户参数设置结果结果
 * 
 * @author scott.liang
 * @version 1.0 7/10/2018
 * @since laxcus 1.0
 */
public class SetMultiUserParameterProduct extends EchoProduct {
	
	private static final long serialVersionUID = 2250222766177743240L;
	
	/** 被刷新的处理单元集合 **/
	private TreeSet<RefreshItem> array = new TreeSet<RefreshItem>();

	/**
	 * 构造默认的多用户参数设置结果结果
	 */
	public SetMultiUserParameterProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析设置用户的最大连接数结果
	 * @param reader 可类化数据读取器
	 */
	public SetMultiUserParameterProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 根据传入的多用户参数设置结果结果，生成它的数据副本
	 * @param that SetMultiUserParameterProduct实例
	 */
	private SetMultiUserParameterProduct(SetMultiUserParameterProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果
	 * @param e RefreshItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(RefreshItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理结果
	 * @param siger 用户签名
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Siger siger, boolean successful) {
		return add(new RefreshItem(siger, successful));
	}

	/**
	 * 保存一批处理结果
	 * @param a RefreshItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<RefreshItem> a) {
		int size = array.size();
		for (RefreshItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批处理结果
	 * @param e SetMultiUserParameterProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SetMultiUserParameterProduct e) {
		return addAll(e.array);
	}

	/**
	 * 根据签名查找结果
	 * @param siger 账号签名
	 * @return 返回刷新单元，或者空指针。
	 */
	public RefreshItem find(Siger siger) {
		for (RefreshItem e : array) {
			if (Laxkit.compareTo(e.getSiger(), siger) == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 输出全部被刷新的处理单元
	 * @return 返回RefreshItem列表
	 */
	public List<RefreshItem> list() {
		return new ArrayList<RefreshItem>(array);
	}

	/**
	 * 统计成员数目
	 * @return 返回被刷新的处理单元成员数目
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
		for (RefreshItem e : array) {
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
			RefreshItem e = new RefreshItem(reader);
			array.add(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SetMultiUserParameterProduct duplicate() {
		return new SetMultiUserParameterProduct(this);
	}

}