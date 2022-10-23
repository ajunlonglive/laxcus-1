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
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 刷新参数结果
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public abstract class RefreshParameterProduct extends EchoProduct {
	
	private static final long serialVersionUID = 2250222766177743240L;
	
	/** 被刷新的处理单元集合 **/
	private TreeSet<RefreshItem> array = new TreeSet<RefreshItem>();

	/**
	 * 构造默认的刷新参数结果
	 */
	protected RefreshParameterProduct() {
		super();
	}

	/**
	 * 根据传入的刷新参数结果，生成它的数据副本
	 * @param that RefreshParameterProduct实例
	 */
	protected RefreshParameterProduct(RefreshParameterProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个处理结果
	 * @param e RefreshItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(RefreshItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个处理结果
	 * @param username 用户签名
	 * @param successful 成功
	 * @return 返回真或者假
	 */
	public boolean add(Siger username, boolean successful) {
		return add(new RefreshItem(username, successful));
	}

	/**
	 * 保存一批处理结果
	 * @param a RefreshItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<RefreshItem> a) {
		int size = array.size();
		for (RefreshItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批处理结果
	 * @param e RefreshParameterProduct实例
	 * @return 返回新增成员数目
	 */
	public int addAll(RefreshParameterProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部被刷新的处理单元
	 * @return 返回RefreshItem列表
	 */
	public List<RefreshItem> list() {
		return new ArrayList<RefreshItem>(array);
	}

	/**
	 * 统计成员数目
	 * @return 返回被刷新的处理单元成员数目
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
		for (RefreshItem e : array) {
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
			RefreshItem e = new RefreshItem(reader);
			array.add(e);
		}
	}

}