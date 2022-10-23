/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.limit;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 限制操作应答结果
 * 
 * @author scott.liang
 * @version 1.0 3/23/2017
 * @since laxcus 1.0
 */
public abstract class DefaultLimitProduct extends EchoProduct {

	private static final long serialVersionUID = 8017019103727835497L;

	/** 限制操作单元集合 **/
	private TreeSet<LimitItem> array = new TreeSet<LimitItem>();

	/**
	 * 构造默认的限制操作应答结果
	 */
	protected DefaultLimitProduct() {
		super();
	}

	/**
	 * 生成限制操作应答结果数据副本
	 * @param that DefaultLimitProduct实例
	 */
	protected DefaultLimitProduct(DefaultLimitProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个限制操作单元
	 * @param e LimitItem实例
	 * @return 成功返回真，否则假
	 */
	public boolean add(LimitItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批限制操作单元
	 * @param a 限制操作单元数组
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<LimitItem> a) {
		int size = array.size();
		for (LimitItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 删除一个限制操作单元
	 * @param e LimitItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(LimitItem e) {
		return array.remove(e);
	}

	/**
	 * 输出全部限制操作单元
	 * @return LimitItem列表
	 */
	public List<LimitItem> list() {
		return new ArrayList<LimitItem>(array);
	}

	/**
	 * 判断包含一个限制操作单元
	 * @param e LimitItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(LimitItem e) {
		return array.contains(e);
	}

	/**
	 * 返回成员数目
	 * @return 成员数目
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
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (LimitItem item : array) {
			writer.writeObject(item);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			LimitItem item = LimitItemCreator.resolve(reader);
			array.add(item);
		}
	}

}
