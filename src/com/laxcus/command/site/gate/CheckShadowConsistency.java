/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.gate;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 检查GATE站点的注册用户和站点编号的一致性。<br>
 * 
 * 管理员登录到BANK子域集群使用，流程：WATCH -> BANK -> GATE
 * 
 * @author scott.liang
 * @version 1.0 7/20/2019
 * @since laxcus 1.0
 */
public class CheckShadowConsistency extends Command {

	private static final long serialVersionUID = 1622299928956068464L;

	/** 指定GATE站点地址 **/
	private TreeSet<Node> array = new TreeSet<Node>();

	/** GATE站点统计，由BANK站点设置。 **/
	private int count = 0;

	/**
	 * 根据检查GATE站点的注册用户和站点编号的一致性，生成数据副本
	 * @param that 检查GATE站点的注册用户和站点编号的一致性
	 */
	private CheckShadowConsistency(CheckShadowConsistency that) {
		super(that);
		array.addAll(that.array);
		count = that.count;
	}

	/**
	 * 构造默认的检查GATE站点的注册用户和站点编号的一致性
	 */
	public CheckShadowConsistency() {
		super();
	}

	/**
	 * 构造检查GATE站点的注册用户和站点编号的一致性，保存一组记录
	 * @param a Node数组
	 */
	public CheckShadowConsistency(List<Node> a) {
		this();
		addAll(a);
	}

	/**
	 * 判断显示全部FRONT详细信息
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return array.size() == 0;
	}

	/**
	 * 保存一个GATE地址
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存全部GATE地址
	 * @param a Node列表
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
	 * 输出GATE地址
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 判断地址包含
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean contains(Node e) {
		return array.contains(e);
	}

	/**
	 * 统计GATE地址数目
	 * @return 地址数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断GATE地址数目空值
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 设置GATE站点统计数，由BANK节点设置
	 * @param i
	 */
	public void setCount(int i) {
		count = i;
	}

	/**
	 * 返回GATE站点统计数
	 * @return 统计数
	 */
	public int getCount() {
		return count;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckShadowConsistency duplicate() {
		return new CheckShadowConsistency(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// GATE节点
		writer.writeInt(array.size());
		for (Node e : array) {
			writer.writeObject(e);
		}
		// 统计数
		writer.writeInt(count);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// GATE节点
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			array.add(e);
		}
		// 统计数
		count = reader.readInt();
	}

}