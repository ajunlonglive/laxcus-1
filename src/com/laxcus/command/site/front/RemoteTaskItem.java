/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 服务器组件分布位置<br><br>
 * 
 * 以命名方式记录一个成员，包括：<br>
 * 1. 阶段命名 <br>
 * 2. 关联的CALL节点 <br>
 * 
 * @author scott.liang
 * @version 1.0 3/4/2022
 * @since laxcus 1.0
 */
public class RemoteTaskItem implements Classable, Cloneable, Serializable , Comparable<RemoteTaskItem>{
	
	private static final long serialVersionUID = -8263204483980984634L;
	
	/** 阶段命名 **/
	private Phase phase;

	/** CALL节点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();
	
	/**
	 * 构造默认的服务器组件分布位置
	 */
	public RemoteTaskItem() {
		super();
	}

	/**
	 * 构造服务器组件分布位置，指定分布任务组件类型 
	 * @param taskFamily 分布任务组件类型
	 */
	public RemoteTaskItem(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 构造服务器组件分布位置
	 * @param phase
	 * @param nodes
	 */
	public RemoteTaskItem(Phase phase, Collection<Node> nodes) {
		this(phase);
		addAll(nodes);
	}

	/**
	 * 构造服务器组件分布位置
	 * @param phase
	 * @param nodes
	 */
	public RemoteTaskItem(Phase phase, Node[] nodes) {
		this(phase);
		addAll(nodes);
	}

	/**
	 * 生成服务器组件分布位置副本
	 * @param that 服务器组件分布位置
	 */
	private RemoteTaskItem(RemoteTaskItem that) {
		this();
		phase = that.phase;
		sites.addAll(that.sites);
	}

	/**
	 * 从可类化读取器中解析服务器组件分布位置
	 * @param reader 可类化数据读取器
	 */
	public RemoteTaskItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置阶段命名
	 * @param e
	 */
	public void setPhase(Phase e) {
		Laxkit.nullabled(e);
		phase = e;
	}
	
	/**
	 * 返回阶段命名
	 * @return
	 */
	public Phase getPhase() {
		return phase;
	}
	
	/**
	 * 保存CALL节点地址
	 * @param node CALL节点地址
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node) {
		Laxkit.nullabled(node);
		return sites.add(node);
	}
	
	/**
	 * 加一批节点
	 * @param nodes
	 * @return
	 */
	public int addAll(Node[] nodes) {
		int size = sites.size();
		for (Node node : nodes) {
			add(node);
		}
		return sites.size() - size;
	}

	/**
	 * 加一批节点
	 * @param nodes
	 * @return
	 */
	public int addAll(Collection<Node> nodes) {
		int size = sites.size();
		for (Node node : nodes) {
			add(node);
		}
		return sites.size() - size;
	}
	
	/**
	 * 删除CALL节点地址
	 * @param node CALL节点地址
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(Node node) {
		Laxkit.nullabled(node);
		return sites.remove(node);
	}

	/**
	 * 输出CALL节点地址
	 * @return Node集合
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		// 阶段命名
		writer.writeObject(phase);
		// CALL节点地址
		writer.writeInt(sites.size());
		for (Node e : sites) {
			writer.writeObject(e);
		}

		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		// 阶段命名
		phase = new Phase(reader);
		// CALL节点地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}

		// 统计读取字节数
		return reader.getSeek() - seek;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return RemoteTaskItem实例
	 */
	public RemoteTaskItem duplicate() {
		return new RemoteTaskItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != RemoteTaskItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((RemoteTaskItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return phase.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RemoteTaskItem that) {
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(phase, that.phase);
	}

}