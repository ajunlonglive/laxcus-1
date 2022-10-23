/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.fast;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 数据块快速处理命令
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public abstract class FastMass extends FastSpace {

	private static final long serialVersionUID = -692231837901609972L;

	/** 目标地址集合 */
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造数据块快速处理命令
	 */
	protected FastMass() {
		super();
	}

	/**
	 * 根据传入的数据块操作命令，生成它的数据副本
	 * @param that FastMass实例
	 */
	protected FastMass(FastMass that) {
		super(that);
		array.addAll(that.array);
	}
	
	/**
	 * 输出全部目标站点
	 * @return Node数组
	 */
	public Node[] getSites() {
		Node[] s = new Node[array.size()];
		return array.toArray(s);
	}

	/**
	 * 保存一个目标站点地址
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSite(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批站点地址
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addSites(Collection<Node> a) {
		int size = array.size();
		for(Node e : a) {
			addSite(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 清除站点
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 将数据资源命令参数写入可类化存储器
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 地址成员数目
		writer.writeInt(array.size());
		// 保存地址
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器中解析数据资源命令参数
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 地址成员数目
		int size = reader.readInt();
		// 解析地址
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			this.array.add(e);
		}
	}

}
