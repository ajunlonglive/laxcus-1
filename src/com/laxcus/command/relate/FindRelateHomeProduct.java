/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;

import java.util.*;

import com.laxcus.site.Node;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 关联HOME站点查询结果。
 * 
 * @author scott.liang
 * @version 1.1 10/29/2015
 * @since laxcus 1.1
 */
public class FindRelateHomeProduct extends EchoProduct {
	
	private static final long serialVersionUID = 8693842870906872268L;
	
	/** HOME站点集合 **/
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造默认的关联HOME站点查询结果。
	 */
	public FindRelateHomeProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析实例
	 * @param reader 可类化读取器
	 */
	public FindRelateHomeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据传入的实例生成它的数据副本 FindRelateHomeProduct实例
	 * @param that
	 */
	private FindRelateHomeProduct(FindRelateHomeProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个HOME站点
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 删除一个HOME站点
	 * @param e Node实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Node e) {
		return array.remove(e);
	}

	/**
	 * 保存一批HOME站点
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = array.size();
		if (a != null) {
			for (Node e : a) {
				add(e);
			}
		}
		return array.size() - size;
	}

	/**
	 * 返回列表
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 统计节点数目 
	 * @return 节点数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public FindRelateHomeProduct duplicate() {
		return new FindRelateHomeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Node e : array) {
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
			Node e = new Node(reader);
			array.add(e);
		}
	}

}