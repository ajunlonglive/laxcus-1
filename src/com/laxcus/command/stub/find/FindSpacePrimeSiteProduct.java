/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 站点地址集合。
 * 
 * @author scott.liang
 * @version 1.1 8/18/2015
 * @since laxcus 1.0
 */
public final class FindSpacePrimeSiteProduct extends EchoProduct {

	private static final long serialVersionUID = 2161080587701326365L;

	/** 站点地址集合 **/
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造站点地址集合的浅层副本
	 * @param that SiteProduct实例
	 */
	private FindSpacePrimeSiteProduct(FindSpacePrimeSiteProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 构造站点地址集合
	 */
	public FindSpacePrimeSiteProduct() {
		super();
	}

	/**
	 * 从可类化读取器中解析站点地址集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindSpacePrimeSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存站点地址，不允许空指针
	 * @param e Node实例
	 * @return 成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批站点地址
	 * @param a  Node列表
	 * @return 返回新增站点数目
	 */
	public int addAll(List<Node> a) {
		int size = array.size();
		for (Node e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存站点地址数组
	 * @param a Node数组
	 * @return 返回新增站点数目
	 */
	public int addAll(Node[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部站点
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 统计站点数目
	 * @return 站点数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public FindSpacePrimeSiteProduct duplicate() {
		return new FindSpacePrimeSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Node node : array) {
			writer.writeObject(node);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node node = new Node(reader);
			array.add(node);
		}
	}

}