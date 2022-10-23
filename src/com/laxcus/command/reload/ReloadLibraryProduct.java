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
 * 重新加载动态链接库执行结果
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class ReloadLibraryProduct extends EchoProduct {

	private static final long serialVersionUID = -6240611532606647517L;

	/** 单元集合 **/
	private TreeSet<ReloadLibraryItem> array = new TreeSet<ReloadLibraryItem>();

	/**
	 * 构造默认的重新加载动态链接库执行结果
	 */
	public ReloadLibraryProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器解析重新加载动态链接库执行结果
	 * @param reader 可类化数据读取器
	 */
	public ReloadLibraryProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造重新加载动态链接库执行结果的数据副本
	 * @param that ReloadLibraryProduct实例
	 */
	private ReloadLibraryProduct(ReloadLibraryProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个执行结果单元，不允许空指针
	 * @param e ReloadLibraryItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(ReloadLibraryItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个执行结果单元
	 * @param node 站点地址
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node) {
		return add(new ReloadLibraryItem(node));
	}

	/**
	 * 保存一批执行结果单元
	 * @param a ReloadLibraryItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<ReloadLibraryItem> a) {
		int size = array.size();
		for(ReloadLibraryItem e: a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批执行结果单元
	 * @param e ReloadLibraryProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(ReloadLibraryProduct e) {
		return addAll(e.list());
	}

	/**
	 * 删除一个执行结果单元
	 * @param e ReloadLibraryItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(ReloadLibraryItem e) {
		return array.remove(e);
	}

	/**
	 * 判断存在
	 * @param e ReloadLibraryItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ReloadLibraryItem e) {
		return array.contains(e);
	}
	
	/**
	 * 输出全部执行结果单元
	 * @return ReloadLibraryItem列表
	 */
	public List<ReloadLibraryItem> list() {
		return new ArrayList<ReloadLibraryItem>(array);
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
	public ReloadLibraryProduct duplicate() {
		return new ReloadLibraryProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ReloadLibraryItem e : array) {
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
			ReloadLibraryItem item = new ReloadLibraryItem(reader);
			array.add(item);
		}
	}

}