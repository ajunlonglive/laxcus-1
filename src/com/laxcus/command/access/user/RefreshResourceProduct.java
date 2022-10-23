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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 刷新注册资源结果
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public abstract class RefreshResourceProduct extends EchoProduct {

	private static final long serialVersionUID = 8481347923322439366L;

	/** 处理结果 **/
	private TreeSet<RefreshResourceItem> array = new TreeSet<RefreshResourceItem>();

	/**
	 * 构造默认的刷新注册资源结果
	 */
	protected RefreshResourceProduct() {
		super();
	}

	/**
	 * 根据传入的刷新注册资源结果，生成它的数据副本
	 * @param that RefreshResourceProduct实例
	 */
	protected RefreshResourceProduct(RefreshResourceProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果
	 * @param e RefreshResourceItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(RefreshResourceItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理结果
	 * @param site 站点地址
	 * @param siger 用户签名
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Node site, Siger siger, boolean successful) {
		RefreshResourceItem e = new RefreshResourceItem(site, siger, successful);
		return add(e);
	}

	/**
	 * 保存一批处理结果
	 * @param a RefreshResourceItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<RefreshResourceItem> a) {
		int size = array.size();
		for(RefreshResourceItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批处理结果
	 * @param e RefreshResourceProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(RefreshResourceProduct e) {
		return addAll(e.array);
	}

	/**
	 * 查找某个签名的全部单元
	 * @param siger 用户签名
	 * @return 单元数组
	 */
	public List<RefreshResourceItem> findAll(Siger siger) {
		ArrayList<RefreshResourceItem> a = new ArrayList<RefreshResourceItem>();
		for (RefreshResourceItem e : array) {
			if (Laxkit.compareTo(e.getSiger(), siger) == 0) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 输出全部RefreshResourceItem单元
	 * @return 返回RefreshResourceItem列表
	 */
	public List<RefreshResourceItem> list() {
		return new ArrayList<RefreshResourceItem>(array);
	}

	/**
	 * 统计RefreshResourceItem成员数目
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
		for (RefreshResourceItem e : array) {
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
			RefreshResourceItem e = new RefreshResourceItem(reader);
			array.add(e);
		}
	}

}