/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.site.Node;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 映像数据块站点报告。<br>
 * 这个回显结果对应“查询映像数据块站点命令”。
 * 
 * @author scott.liang
 * @version 1.1 7/10/2015
 * @since laxcus 1.0
 */
public final class ReflexStubSiteProduct extends EchoProduct {
	
	private static final long serialVersionUID = -681267627833756679L;
	
	/** DATA站点集合 **/
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造默认的映像数据块站点报告。
	 */
	public ReflexStubSiteProduct() {
		super();
	}

	/**
	 * 根据传入的映像数据块站点报告实例生成它的浅层数据副本
	 * @param that ReflexStubSiteProduct实例
	 */
	private ReflexStubSiteProduct(ReflexStubSiteProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个DATA站点
	 * @param e DATA站点
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批DATA站点
	 * @param a DATA站点集合
	 * @return 返回新增加的站点数目
	 */
	public int addAll(Collection<Node> a) {
		int size = array.size();
		for(Node e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存一批DATA站点
	 * @param that
	 */
	public void addAll(ReflexStubSiteProduct that) {
		addAll(that.array);
	}

	/**
	 * 删除DATA站点
	 * @param e DATA站点
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Node e) {
		return array.remove(e);
	}

	/**
	 * 输出全部DATA站点
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 统计DATA站点数目 
	 * @return DATA站点数目
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

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ReflexStubSiteProduct duplicate() {
		return new ReflexStubSiteProduct(this);
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