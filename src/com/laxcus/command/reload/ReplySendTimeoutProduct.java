/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reload;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 发送异步数据超时执行结果
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class ReplySendTimeoutProduct extends EchoProduct {

	private static final long serialVersionUID = -7381845841033461734L;

	/** 单元集合 **/
	private TreeSet<ReplySendTimeoutItem> array = new TreeSet<ReplySendTimeoutItem>();

	/**
	 * 构造默认的发送异步数据超时执行结果
	 */
	public ReplySendTimeoutProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析发送异步数据超时执行结果
	 * @param reader 可类化数据读取器
	 */
	public ReplySendTimeoutProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造发送异步数据超时执行结果的数据副本
	 * @param that ReplySendTimeoutProduct实例
	 */
	private ReplySendTimeoutProduct(ReplySendTimeoutProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造发送异步数据超时执行结果，指定站点地址和成功标记
	 * @param node 站点地址
	 * @param successful 成功标记
	 */
	public ReplySendTimeoutProduct(Node node, boolean successful) {
		this();
		add(new ReplySendTimeoutItem(node, successful));
	}
	
	/**
	 * 有选择保存单元，判断过程：<br>
	 * 1. 两个节点地址必须一致。<br>
	 * 2. 同地址情况下，保存成功，丢弃失败的。<br><br>
	 * 
	 * @param that 传入的单元
	 * @return 保存成功返回真，否则假
	 */
	private boolean push(ReplySendTimeoutItem that) {
		Laxkit.nullabled(that);

		ReplySendTimeoutItem next = null;
		for (ReplySendTimeoutItem old : array) {
			// 基于节点地址的比较
			boolean math = (Laxkit.compareTo(old.getSite(), that.getSite()) == 0);
			if (!math) {
				continue;
			}

			// 原有不成功，新的成功，把旧的删除，保存新的。退出
			if (!old.isSuccessful() && that.isSuccessful()) {
				next = old;
				break;
			} 
			// 旧的成功，新的不成功，退出不保存
			else if(old.isSuccessful() && that.isSuccessful()) {
				return false;
			}
		}
		// 保存!
		if (next != null) {
			array.remove(next);
		} 
		return	array.add(that);
	}
	
	/**
	 * 保存一个执行结果单元，不允许空指针
	 * @param e ReplySendTimeoutItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ReplySendTimeoutItem e) {
		Laxkit.nullabled(e);

		return push(e);
	}

	/**
	 * 保存一个执行结果单元
	 * @param node 站点地址
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, boolean success) {
		return add(new ReplySendTimeoutItem(node, success));
	}

	/**
	 * 保存一批执行结果单元
	 * @param a ReplySendTimeoutItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<ReplySendTimeoutItem> a) {
		int size = array.size();
		for(ReplySendTimeoutItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e ReplySendTimeoutProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ReplySendTimeoutProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个执行结果单元
	 * @param e ReplySendTimeoutItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(ReplySendTimeoutItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ReplySendTimeoutItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ReplySendTimeoutItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return ReplySendTimeoutItem列表
	 */
	public List<ReplySendTimeoutItem> list() {
		return new ArrayList<ReplySendTimeoutItem>(array);
	}

	/**
	 * 统计执行结果单元成员数目
	 * @return 单元成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ReplySendTimeoutProduct duplicate() {
		return new ReplySendTimeoutProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ReplySendTimeoutItem e : array) {
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
			ReplySendTimeoutItem item = new ReplySendTimeoutItem(reader);
			array.add(item);
		}
	}

}