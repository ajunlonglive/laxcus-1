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
 * 修改应答包尺寸执行结果
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class ReplyPacketSizeProduct extends EchoProduct {

	private static final long serialVersionUID = -6240611532606647517L;

	/** 单元集合 **/
	private TreeSet<ReplyPacketSizeItem> array = new TreeSet<ReplyPacketSizeItem>();

	/**
	 * 构造默认的修改应答包尺寸执行结果
	 */
	public ReplyPacketSizeProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析修改应答包尺寸执行结果
	 * @param reader 可类化数据读取器
	 */
	public ReplyPacketSizeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造修改应答包尺寸执行结果的数据副本
	 * @param that ReplyPacketSizeProduct实例
	 */
	private ReplyPacketSizeProduct(ReplyPacketSizeProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造修改应答包尺寸执行结果，指定站点地址和成功标记
	 * @param node 站点地址
	 * @param successful 成功标记
	 */
	public ReplyPacketSizeProduct(Node node, boolean successful) {
		this();
		add(new ReplyPacketSizeItem(node, successful));
	}
	
	/**
	 * 有选择保存单元，判断过程：<br>
	 * 1. 两个节点地址必须一致。<br>
	 * 2. 同地址情况下，保存成功，丢弃失败的。<br><br>
	 * 
	 * @param that 传入的单元
	 * @return 保存成功返回真，否则假
	 */
	private boolean push(ReplyPacketSizeItem that) {
		Laxkit.nullabled(that);

		ReplyPacketSizeItem next = null;
		for (ReplyPacketSizeItem old : array) {
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
	 * @param e ReplyPacketSizeItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ReplyPacketSizeItem e) {
		Laxkit.nullabled(e);

		return push(e);
	}

	/**
	 * 保存一个执行结果单元
	 * @param node 站点地址
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, boolean success) {
		return add(new ReplyPacketSizeItem(node, success));
	}

	/**
	 * 保存一批执行结果单元
	 * @param a ReplyPacketSizeItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<ReplyPacketSizeItem> a) {
		int size = array.size();
		for(ReplyPacketSizeItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e ReplyPacketSizeProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ReplyPacketSizeProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个执行结果单元
	 * @param e ReplyPacketSizeItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(ReplyPacketSizeItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ReplyPacketSizeItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ReplyPacketSizeItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return ReplyPacketSizeItem列表
	 */
	public List<ReplyPacketSizeItem> list() {
		return new ArrayList<ReplyPacketSizeItem>(array);
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
	public ReplyPacketSizeProduct duplicate() {
		return new ReplyPacketSizeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ReplyPacketSizeItem e : array) {
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
			ReplyPacketSizeItem item = new ReplyPacketSizeItem(reader);
			array.add(item);
		}
	}

}