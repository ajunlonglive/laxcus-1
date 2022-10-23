/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 设置虚拟空间参数应答报告
 * 
 * @author scott.liang
 * @version 1.0 10/29/2019
 * @since laxcus 1.0
 */
public class VirtualCyberProduct extends EchoProduct {

	private static final long serialVersionUID = -8058286037535380057L;

	/** 单元数组 **/
	private TreeSet<VirtualCyberItem> array = new TreeSet<VirtualCyberItem>();

	/**
	 * 构造默认的设置虚拟空间参数应答报告
	 */
	public VirtualCyberProduct() {
		super();
	}

	/**
	 * 构造设置虚拟空间参数应答报告，指定单元
	 * @param item 单元
	 */
	public VirtualCyberProduct(VirtualCyberItem item) {
		this();
		add(item);
	}

	/**
	 * 从可类化数据读取器解析设置虚拟空间参数应答报告
	 * @param reader 可类化数据读取器
	 */
	public VirtualCyberProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造设置虚拟空间参数应答报告的数据副本
	 * @param that 设置虚拟空间参数应答报告
	 */
	private VirtualCyberProduct(VirtualCyberProduct that) {
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
	private boolean push(VirtualCyberItem that) {
		Laxkit.nullabled(that);

		VirtualCyberItem next = null;
		for (VirtualCyberItem old : array) {
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
	 * @param e VirtualCyberItem实例
	 * @return 返回真或者假
	 */
	public boolean add(VirtualCyberItem e) {
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
		return add(new VirtualCyberItem(node, successful));
	}

	/**
	 * 保存一批处理单元
	 * @param a VirtualCyberItem列表
	 * @return 返回新增成员数目
	 */
	public int addAll(List<VirtualCyberItem> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (VirtualCyberItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理单元
	 * @param e VirtualCyberProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(VirtualCyberProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个处理单元
	 * @param e VirtualCyberItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(VirtualCyberItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e VirtualCyberItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(VirtualCyberItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部处理单元列表
	 * @return VirtualCyberItem列表
	 */
	public List<VirtualCyberItem> list() {
		return new ArrayList<VirtualCyberItem>(array);
	}

	/**
	 * 统计处理单元成员数目
	 * @return 处理单元成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public VirtualCyberProduct duplicate() {
		return new VirtualCyberProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (VirtualCyberItem e : array) {
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
			VirtualCyberItem item = new VirtualCyberItem(reader);
			array.add(item);
		}
	}

}