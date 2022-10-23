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
 * 节点最大CPU使用率限制报告
 * 
 * @author scott.liang
 * @version 1.0 9/8/2019
 * @since laxcus 1.0
 */
public class MostCPUProduct extends EchoProduct {

	private static final long serialVersionUID = 1269506512440159821L;

	/** 单元数组 **/
	private TreeSet<MostCPUItem> array = new TreeSet<MostCPUItem>();

	/**
	 * 构造默认的节点最大CPU使用率限制报告
	 */
	public MostCPUProduct() {
		super();
	}

	/**
	 * 构造节点最大CPU使用率限制报告，指定单元
	 * @param item 单元
	 */
	public MostCPUProduct(MostCPUItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器解析节点最大CPU使用率限制报告
	 * @param reader 可类化数据读取器
	 */
	public MostCPUProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造节点最大CPU使用率限制报告的数据副本
	 * @param that 节点最大CPU使用率限制报告
	 */
	private MostCPUProduct(MostCPUProduct that) {
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
	private boolean push(MostCPUItem that) {
		Laxkit.nullabled(that);

		MostCPUItem next = null;
		for (MostCPUItem old : array) {
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
	 * 保存一个处理单元，不允许空指针
	 * @param e MostCPUItem实例
	 * @return 返回真或者假
	 */
	public boolean add(MostCPUItem e) {
		Laxkit.nullabled(e);
		
		return push(e);
	}
	
	/**
	 * 保存一个处理单元
	 * @param node Node实例
	 * @param successful 成功标记
	 * @return 返回真或者假
	 */
	public boolean add(Node node, boolean successful) {
		return add(new MostCPUItem(node, successful));
	}

	/**
	 * 保存一批处理单元
	 * @param a MostCPUItem列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<MostCPUItem> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (MostCPUItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理单元
	 * @param e MostCPUProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(MostCPUProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个处理单元
	 * @param e MostCPUItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(MostCPUItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e MostCPUItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(MostCPUItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部处理单元列表
	 * @return MostCPUItem列表
	 */
	public List<MostCPUItem> list() {
		return new ArrayList<MostCPUItem>(array);
	}

	/**
	 * 统计处理单元成员数目
	 * @return 处理单元成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 返回全部节点
	 * @return Node列表
	 */
	public List<Node> getSites() {
		ArrayList<Node> a = new ArrayList<Node>();
		for (MostCPUItem e : array) {
			a.add(e.getSite());
		}
		return a;
	}
	
	/**
	 * 找到匹配的单元
	 * @param node 节点
	 * @return 返回实例，没有是空指针
	 */
	public MostCPUItem find(Node node) {
		for (MostCPUItem e : array) {
			boolean b = (Laxkit.compareTo(e.getSite(), node) == 0);
			if (b) {
				return e;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public MostCPUProduct duplicate() {
		return new MostCPUProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (MostCPUItem e : array) {
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
			MostCPUItem item = new MostCPUItem(reader);
			array.add(item);
		}
	}

}