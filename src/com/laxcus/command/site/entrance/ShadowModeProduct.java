/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.entrance;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 定位GATE站点模式结果
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public class ShadowModeProduct extends EchoProduct {
	
	private static final long serialVersionUID = -8461572779734210474L;

	/** 返回的节点 **/
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造默认的定位GATE站点模式
	 */
	public ShadowModeProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析定位GATE站点模式
	 * @param reader 可类化数据读取器
	 */
	public ShadowModeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * @param that
	 */
	private ShadowModeProduct(ShadowModeProduct that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 保存一个ENTRANCE节点
	 * @param e ENTRANCE节点
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一批ENTRANCE节点
	 * @param a ENTRANCE节点集合
	 * @return 新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 保存一批ENTRANCE节点
	 * @param a ENTRANCE节点集合
	 * @return 新增成员数目
	 */
	public int addAll(ShadowModeProduct e) {
		return addAll(e.array);
	}
	
	/**
	 * 输出全部
	 * @return 全部参数
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 成员数目
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
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ShadowModeProduct duplicate() {
		return new ShadowModeProduct(this);
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
			add(e);
		}
	}

}