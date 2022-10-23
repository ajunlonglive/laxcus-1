/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 打印站点实时追踪检测目录 <br>
 * 
 * WATCH/FRONT站点使用，WATCH站点可以检测集群所有站点，FRONT只能操作自己。
 * 
 * @author scott.liang
 * @version 1.0 8/18/2019
 * @since laxcus 1.0
 */
public final class CheckSitePath extends Command {

	private static final long serialVersionUID = 4154761730131283863L;

	/** 设置为本地 **/
	private boolean local;
	
	/** 目标站点地址 **/
	private ArrayList<Node> sites = new ArrayList<Node>();

	/**
	 * 构造打印站点实时追踪检测目录
	 */
	public CheckSitePath() {
		super();
		setLocal(false);
	}

	/**
	 * 从可类化数据读取器中解析打印站点实时追踪检测目录
	 * @param reader 可类化数据读取器
	 */
	public CheckSitePath(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成命令副本
	 * @param that CheckSitePath实例
	 */
	private CheckSitePath(CheckSitePath that) {
		super(that);
		local = that.local;
		sites.addAll(that.sites);
	}
	
	/**
	 * 设置为本地站点
	 * @param b 真或者假
	 */
	public void setLocal(boolean b) {
		local = b;
	}

	/**
	 * 判断是本地站点
	 * @return 返回真或者假
	 */
	public boolean isLocal() {
		return local;
	}

	/**
	 * 保存一个站点地址，不允许空指针
	 * @param e Node实例
	 * @return 返回新增成员数目
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		// 存在，忽略它
		if (sites.contains(e)) {
			return false;
		}

		return sites.add(e);
	}

	/**
	 * 返回站点地址
	 * @return 站点列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 设置一批站点地址
	 * @param a Node集合
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 返回站点数目
	 * @return 站点数目
	 */
	public int size() {
		return sites.size();
	}
	
	/**
	 * 清除
	 */
	public void clear() {
		sites.clear();
	}

	/**
	 * 判断站点是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 判断处理全部站点
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return size() == 0 && !local;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckSitePath duplicate() {
		return new CheckSitePath(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(local);
		// 保存地址
		writer.writeInt(sites.size());
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		local = reader.readBoolean();
		// 解析地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}
	}

}