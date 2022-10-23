/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 作业节点
 * 
 * @author scott.liang
 * @version 1.0 9/21/2022
 * @since laxcus 1.0
 */
public class CheckJobSiteProduct extends EchoProduct {

	private static final long serialVersionUID = 5654492133412452208L;

	/** 数组 **/
	private TreeSet<Node> array = new TreeSet<Node>();
	
	/**
	 * 构造默认的作业节点
	 */
	public CheckJobSiteProduct() {
		super();
	}
	
	/**
	 * 生成作业节点副本
	 * @param that
	 */
	private CheckJobSiteProduct(CheckJobSiteProduct that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 从可类化读取器中解析作业节点
	 * @param reader 可类化读取器
	 */
	public CheckJobSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加一个节点
	 * @param e 节点实例
	 */
	public void add(Node e) {
		Laxkit.nullabled(e);
		if (!array.contains(e)) {
			array.add(e);
		}
	}
	
	/**
	 * 增加一批节点
	 * @param a
	 * @return
	 */
	public int addAll(Collection<Node> a) {
		int size = array.size();
		if (a != null) {
			array.addAll(a);
		}
		return array.size() - size;
	}

	/**
	 * 增加一批节点
	 * @param e
	 * @return
	 */
	public int addAll(CheckJobSiteProduct e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部节点
	 * @return
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 全部成员数
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckJobSiteProduct duplicate() {
		return new CheckJobSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
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