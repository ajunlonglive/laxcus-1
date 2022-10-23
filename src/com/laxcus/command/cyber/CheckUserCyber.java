/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检测集群中用户虚拟空间。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public final class CheckUserCyber extends Command {

	private static final long serialVersionUID = -1316931089783736159L;

	/** 指定的站点 */
	private TreeSet<Node> array = new TreeSet<Node>();

	/**
	 * 构造默认的检测集群中用户虚拟空间
	 */
	public CheckUserCyber() {
		super();
	}

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that CheckUserCyber实例
	 */
	private CheckUserCyber(CheckUserCyber that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析检测集群中用户虚拟空间
	 * @param reader 可类化数据读取器
	 */
	public CheckUserCyber(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckUserCyber duplicate() {
		return new CheckUserCyber(this);
	}

	/**
	 * 判断是操作全部站点
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.isEmpty();
	}

	/**
	 * 保存一个站点，不允许空指针
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除一个节点地址
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean remove(Node e) {
		return array.remove(e);
	}

	/**
	 * 保存一批节点地址
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Node> a) {
		int size = array.size();
		for (Node e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回站点列表
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 清除全部站点
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 统计站点数目
	 * @return 站点数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集。如果这里为空，表示设置全部DATA主站点下的某个DSM表
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 地址成员数目
		writer.writeInt(array.size());
		// 保存地址
		for (Node e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 地址成员数目
		int size = reader.readInt();
		// 解析地址
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
	}

}