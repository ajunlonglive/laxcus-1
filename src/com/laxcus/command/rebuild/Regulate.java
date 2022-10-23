/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.*;

/**
 * 数据优化命令。<br>
 * 
 * 命令格式：REGULATE schema.table [ORDER BY [column-name]] [TO [ data site, data site, ...]] <br>
 * 
 * 数据优化只针对一个表，在DATA主站点，使用Access.regulate方法执行。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/30/2015
 * @since laxcus 1.0
 */
public final class Regulate extends Command {

	private static final long serialVersionUID = -3889399253279531871L;

	/** 列空间 **/
	private Dock dock;

	/** 目标地址集合 */
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 根据传入的数据表优化命令，生成它的数据副本
	 * @param that Regulate实例
	 */
	private Regulate(Regulate that) {
		super(that);	
		dock = that.dock;
		sites.addAll(that.sites);
	}

	/**
	 * 构造默认的数据表优化命令。
	 */
	private Regulate() {
		super();
	}

	/**
	 * 构造数据表优化命令，指定列空间
	 * @param dock Dock实例
	 */
	public Regulate(Dock dock) {
		this();
		setDock(dock);
	}

	/**
	 * 构造数据表优化命令，指定数据表名和列编号
	 * @param space 数据表名
	 * @param columnId 列编号
	 */
	public Regulate(Space space, short columnId) {
		this(new Dock(space, columnId));
	}

	/**
	 * 构造数据表优化命令，指定数据表名
	 * @param space 数据表名
	 */
	public Regulate(Space space) {
		this(space, (short) 0);
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Regulate(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 保存一个站点地址，不允许空指针
	 * @param e Node实例
	 * @return 返回新增成员数目
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return sites.add(e);
	}

	/**
	 * 返回站点地址
	 * @return Node数组
	 */
	public List<Node> getSites() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 设置一批DATA站点地址
	 * @param a Node数组
	 * @return 返回新增成员数目
	 */
	public int addSites(Collection<Node> a) {
		int size = sites.size();
		for (Node e : a) {
			add(e);
		}
		return sites.size() - size;
	}

	/**
	 * 清除地址
	 */
	public void clearSites() {
		sites.clear();
	}
	
	/**
	 * 设置列空间
	 * @param e Dock实例
	 */
	public void setDock(Dock e) {
		Laxkit.nullabled(e);

		dock = e;
	}
	
	/**
	 * 返回列空间
	 * @return Dock实例
	 */
	public Dock getDock() {
		return dock;
	}

	/**
	 * 设置索引键
	 * @param id 索引键
	 */
	public void setColumnId(short id) {
		dock.setColumnId(id);
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		dock.setSpace(e);
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return dock.getSpace();
	}

	/**
	 * 返回索引键
	 * @return 索引键
	 */
	public short getColumnId() {
		return dock.getColumnId();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Regulate duplicate() {
		return new Regulate(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(dock);
		// 地址成员数目
		writer.writeInt(sites.size());
		// 保存地址
		for (Node e : sites) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 列空间
		dock = new Dock(reader);
		// 地址成员数目
		int size = reader.readInt();
		// 解析地址
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			this.sites.add(e);
		}
	}

}