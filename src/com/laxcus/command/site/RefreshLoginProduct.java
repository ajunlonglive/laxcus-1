/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 强制注册处理结果
 * 
 * @author scott.liang
 * @version 1.0 5/11/2017
 * @since laxcus 1.0
 */
public class RefreshLoginProduct extends EchoProduct {

	private static final long serialVersionUID = 4096536524958413244L;

	/** 处理结果 **/
	private TreeSet<RefreshLoginItem> array = new TreeSet<RefreshLoginItem>();

	/**
	 * 构造默认的强制注册处理结果
	 */
	public RefreshLoginProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析强制注册处理结果
	 * @param reader 可类化数据读取器
	 */
	public RefreshLoginProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 根据传入的强制注册处理结果，生成它的数据副本
	 * @param that RefreshLoginProduct实例
	 */
	private RefreshLoginProduct(RefreshLoginProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果，不允许空指针
	 * @param e RefreshLoginItem实例
	 * @return 返回真或者假
	 */
	public boolean add(RefreshLoginItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理结果
	 * @param site 站点地址
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Node site, boolean successful) {
		return add(new RefreshLoginItem(site, successful));
	}

	/**
	 * 保存一批处理结果
	 * @param a RefreshLoginItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<RefreshLoginItem> a) {
		int size = array.size();
		for (RefreshLoginItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批处理结果
	 * @param e RefreshLoginProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(RefreshLoginProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部处理结果
	 * @return RefreshLoginItem列表
	 */
	public List<RefreshLoginItem> list() {
		return new ArrayList<RefreshLoginItem>(array);
	}

	/**
	 * 统计成员数目
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
		for (RefreshLoginItem e : array) {
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
			RefreshLoginItem e = new RefreshLoginItem(reader);
			array.add(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public RefreshLoginProduct duplicate() {
		return new RefreshLoginProduct(this);
	}

}