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
 * 据流接收队列成员执行结果
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public class ReplyFlowControlProduct extends EchoProduct {

	private static final long serialVersionUID = 7257353650126044664L;

	/** 单元集合 **/
	private TreeSet<ReplyFlowControlItem> array = new TreeSet<ReplyFlowControlItem>();

	/**
	 * 构造默认的据流接收队列成员执行结果
	 */
	public ReplyFlowControlProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析据流接收队列成员执行结果
	 * @param reader 可类化数据读取器
	 */
	public ReplyFlowControlProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造据流接收队列成员执行结果的数据副本
	 * @param that ReplyFlowControlProduct实例
	 */
	private ReplyFlowControlProduct(ReplyFlowControlProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造据流接收队列成员执行结果，指定站点地址和成功标记
	 * @param node 站点地址
	 * @param successful 成功标记
	 */
	public ReplyFlowControlProduct(Node node, boolean successful) {
		this();
		add(new ReplyFlowControlItem(node, successful));
	}
	
	/**
	 * 有选择保存单元，判断过程：<br>
	 * 1. 两个节点地址必须一致。<br>
	 * 2. 同地址情况下，保存成功，丢弃失败的。<br><br>
	 * 
	 * @param that 传入的单元
	 * @return 保存成功返回真，否则假
	 */
	private boolean push(ReplyFlowControlItem that) {
		Laxkit.nullabled(that);

		ReplyFlowControlItem next = null;
		for (ReplyFlowControlItem old : array) {
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
	 * @param e ReplyFlowControlItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ReplyFlowControlItem e) {
		Laxkit.nullabled(e);

		return push(e);
	}

	/**
	 * 保存一个执行结果单元
	 * @param node 站点地址
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, boolean success) {
		return add(new ReplyFlowControlItem(node, success));
	}

	/**
	 * 保存一批执行结果单元
	 * @param a ReplyFlowControlItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<ReplyFlowControlItem> a) {
		int size = array.size();
		for(ReplyFlowControlItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e ReplyFlowControlProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ReplyFlowControlProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个执行结果单元
	 * @param e ReplyFlowControlItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(ReplyFlowControlItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ReplyFlowControlItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ReplyFlowControlItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return ReplyFlowControlItem列表
	 */
	public List<ReplyFlowControlItem> list() {
		return new ArrayList<ReplyFlowControlItem>(array);
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
	public ReplyFlowControlProduct duplicate() {
		return new ReplyFlowControlProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ReplyFlowControlItem e : array) {
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
			ReplyFlowControlItem item = new ReplyFlowControlItem(reader);
			array.add(item);
		}
	}

}