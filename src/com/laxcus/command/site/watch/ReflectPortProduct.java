/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置映射端口执行结果
 * 
 * @author scott.liang
 * @version 1.0 10/22/2020
 * @since laxcus 1.0
 */
public class ReflectPortProduct extends EchoProduct {

	private static final long serialVersionUID = 7018695570888497163L;

	/** 单元集合 **/
	private TreeSet<ReflectPortItem> array = new TreeSet<ReflectPortItem>();

	/**
	 * 构造默认的设置映射端口执行结果
	 */
	public ReflectPortProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析设置映射端口执行结果
	 * @param reader 可类化数据读取器
	 */
	public ReflectPortProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造设置映射端口执行结果的数据副本
	 * @param that ReflectPortProduct实例
	 */
	private ReflectPortProduct(ReflectPortProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一组执行结果
	 * @param that 副本
	 */
	public void add(ReflectPortProduct that) {
		array.addAll(that.array);
	}

	/**
	 * 保存一个执行结果单元，不允许空指针
	 * @param e ReflectPortItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ReflectPortItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批执行结果单元
	 * @param a ReflectPortItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<ReflectPortItem> a) {
		int size = array.size();
		for(ReflectPortItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e ReflectPortProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ReflectPortProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个执行结果单元
	 * @param e ReflectPortItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(ReflectPortItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ReflectPortItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ReflectPortItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return ReflectPortItem列表
	 */
	public List<ReflectPortItem> list() {
		return new ArrayList<ReflectPortItem>(array);
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
	public ReflectPortProduct duplicate() {
		return new ReflectPortProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ReflectPortItem e : array) {
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
			ReflectPortItem item = new ReflectPortItem(reader);
			array.add(item);
		}
	}

}