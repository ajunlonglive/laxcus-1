/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 重新加载安全服务执行结果
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class FlushSecureTokenProduct extends EchoProduct {

	private static final long serialVersionUID = -6240611532606647517L;

	/** 单元集合 **/
	private TreeSet<FlushSecureTokenItem> array = new TreeSet<FlushSecureTokenItem>();

	/**
	 * 构造默认的重新加载安全服务执行结果
	 */
	public FlushSecureTokenProduct() {
		super();
	}

	/**
	 * 构造重新加载安全服务执行结果，指定站点地址和成功标记
	 * @param node 站点地址
	 * @param successful 成功标记
	 */
	public FlushSecureTokenProduct(Node node, boolean successful) {
		this();
		add(new FlushSecureTokenItem(node, successful));
	}

	/**
	 * 从可类化数据读取器解析重新加载安全服务执行结果
	 * @param reader 可类化数据读取器
	 */
	public FlushSecureTokenProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造重新加载安全服务执行结果的数据副本
	 * @param that ReloadSecureProduct实例
	 */
	private FlushSecureTokenProduct(FlushSecureTokenProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 有选择保存单元，判断过程：<br>
	 * 1. 两个节点地址必须一致。<br>
	 * 2. 同地址情况下，保存成功，丢弃失败的。<br><br>
	 * 
	 * @param that 传入的单元
	 * @return 保存成功返回真，否则假
	 */
	private boolean push(FlushSecureTokenItem that) {
		Laxkit.nullabled(that);

		FlushSecureTokenItem next = null;
		for (FlushSecureTokenItem old : array) {
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
	 * @param e ReloadSecureItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(FlushSecureTokenItem e) {
		Laxkit.nullabled(e);

		return push(e);
	}

	/**
	 * 保存一个执行结果单元
	 * @param node 站点地址
	 * @param successful 成功标记
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, boolean successful) {
		return add(new FlushSecureTokenItem(node, successful));
	}

	/**
	 * 保存一批执行结果单元
	 * @param a ReloadSecureItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<FlushSecureTokenItem> a) {
		int size = array.size();
		for(FlushSecureTokenItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e ReloadSecureProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(FlushSecureTokenProduct e) {
		return addAll(e.list());
	}

	/**
	 * 返回节点
	 * @return
	 */
	public List<Node> getSites() {
		ArrayList<Node> a = new ArrayList<Node>();
		for (FlushSecureTokenItem e : array) {
			a.add(e.getSite());
		}
		return a;
	}

	/**
	 * 查找匹配的...
	 * @param node
	 * @return
	 */
	public FlushSecureTokenItem find(Node node) {
		for (FlushSecureTokenItem e : array) {
			if (Laxkit.compareTo(e.getSite(), node) == 0) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 删除一个执行结果单元
	 * @param e ReloadSecureItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(FlushSecureTokenItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ReloadSecureItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(FlushSecureTokenItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return ReloadSecureItem列表
	 */
	public List<FlushSecureTokenItem> list() {
		return new ArrayList<FlushSecureTokenItem>(array);
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
	public FlushSecureTokenProduct duplicate() {
		return new FlushSecureTokenProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (FlushSecureTokenItem e : array) {
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
			FlushSecureTokenItem item = new FlushSecureTokenItem(reader);
			array.add(item);
		}
	}

}