/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 服务器系统信息检测结果
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class CheckSystemInfoProduct extends EchoProduct {

	private static final long serialVersionUID = 7497904383532055841L;

	/** 单元集合 **/
	private TreeSet<CheckSystemInfoItem> array = new TreeSet<CheckSystemInfoItem>();

	/**
	 * 构造默认的服务器系统信息检测结果
	 */
	public CheckSystemInfoProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析服务器系统信息检测结果
	 * @param reader 可类化数据读取器
	 */
	public CheckSystemInfoProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造服务器系统信息检测结果的数据副本
	 * @param that CheckSystemInfoProduct实例
	 */
	private CheckSystemInfoProduct(CheckSystemInfoProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造服务器系统信息检测结果，指定站点地址和成功标记
	 * @param node 站点地址
	 * @param successful 成功标记
	 */
	public CheckSystemInfoProduct(Node node, boolean successful) {
		this();
		add(node, successful);
	}
	
	/**
	 * 有选择保存单元，判断过程：<br>
	 * 1. 两个节点地址必须一致。<br>
	 * 2. 同地址情况下，保存成功，丢弃失败的。<br><br>
	 * 
	 * @param that 传入的单元
	 * @return 保存成功返回真，否则假
	 */
	private boolean push(CheckSystemInfoItem that) {
		Laxkit.nullabled(that);

		CheckSystemInfoItem next = null;
		for (CheckSystemInfoItem old : array) {
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
	 * @param e CheckSystemInfoItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(CheckSystemInfoItem e) {
		Laxkit.nullabled(e);

		return push(e);
	}

	/**
	 * 保存一个执行结果单元
	 * @param node 站点地址
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, boolean success) {
		return add(new CheckSystemInfoItem(node, success));
	}

	/**
	 * 保存一批执行结果单元
	 * @param a CheckSystemInfoItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<CheckSystemInfoItem> a) {
		int size = array.size();
		for(CheckSystemInfoItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e CheckSystemInfoProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(CheckSystemInfoProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个执行结果单元
	 * @param e CheckSystemInfoItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(CheckSystemInfoItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e CheckSystemInfoItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(CheckSystemInfoItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return CheckSystemInfoItem列表
	 */
	public List<CheckSystemInfoItem> list() {
		return new ArrayList<CheckSystemInfoItem>(array);
	}

	/**
	 * 统计执行结果单元成员数目
	 * @return 单元成员数目
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 查找单元
	 * @param node 节点
	 * @return 返回单元，没有是空指针
	 */
	public CheckSystemInfoItem find(Node node) {
		for (CheckSystemInfoItem e : array) {
			boolean b = (Laxkit.compareTo(e.getSite(), node) == 0);
			if (b) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * 返回全部节点
	 * @return
	 */
	public List<Node> getSites() {
		ArrayList<Node> a = new ArrayList<Node>();
		for (CheckSystemInfoItem e : array) {
			a.add(e.getSite());
		}
		return a;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckSystemInfoProduct duplicate() {
		return new CheckSystemInfoProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (CheckSystemInfoItem e : array) {
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
			CheckSystemInfoItem item = new CheckSystemInfoItem(reader);
			array.add(item);
		}
	}

}