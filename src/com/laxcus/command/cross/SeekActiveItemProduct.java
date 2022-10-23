/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.law.cross.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查询授权单元处理结果
 * 
 * @author scott.liang
 * @version 1.0 8/15/2017
 * @since laxcus 1.0
 */
public class SeekActiveItemProduct extends EchoProduct {

	private static final long serialVersionUID = -8959611753226213623L;

	/** 授权单元集合 **/
	private TreeSet<ActiveItem> array = new TreeSet<ActiveItem>();

	/**
	 * 构造默认的查询授权单元处理结果
	 */
	public SeekActiveItemProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析共享资源处理结果
	 * @param reader 可类化数据读取器
	 */
	public SeekActiveItemProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成查询授权单元处理结果的数据副本
	 * @param that SeekActiveItemProduct实例
	 */
	private SeekActiveItemProduct(SeekActiveItemProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存授权单元
	 * @param e ActiveItem实例
	 * @return 返回真或者假
	 */
	public boolean add(ActiveItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存授权单元
	 * @param conferrer 被授权人签名
	 * @param flag 共享标识
	 * @return 返回真或者假
	 */
	public boolean add(Siger conferrer, CrossFlag flag) {
		return add(new ActiveItem(conferrer, flag));
	}

	/**
	 * 保存一批单元
	 * @param a ActiveItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<ActiveItem> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 保存一批单元
	 * @param a SeekActiveItemProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SeekActiveItemProduct a) {
		return addAll(a.array);
	}

	/**
	 * 输出单元数组
	 * @return 返回ActiveItem列表
	 */
	public List<ActiveItem> list() {
		return new ArrayList<ActiveItem>(array);
	}

	/**
	 * 统计成员数
	 * @return 返回成员数目
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
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SeekActiveItemProduct duplicate() {
		return new SeekActiveItemProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ActiveItem e : array) {
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
			ActiveItem e = new ActiveItem(reader);
			array.add(e);
		}
	}

}