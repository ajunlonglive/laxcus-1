/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 远端阶段命名 <br>
 * 
 * 由阶段命名和站点地址组成
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public final class RemotePhaseItem implements Serializable, Cloneable, Classable, Markable, Comparable<RemotePhaseItem> {

	private static final long serialVersionUID = 5927674944526595271L;

	/** 阶段命名 */
	private Phase phase;

	/** 站点地址 */
	private Node node;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(phase);
		writer.writeObject(node);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		phase = new Phase(reader);
		node = new Node(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入对象生成它的数据副本
	 * @param that RemotePhaseItem实例
	 */
	private RemotePhaseItem(RemotePhaseItem that) {
		this();
		phase = that.phase.duplicate();
		node = that.node.duplicate();
	}

	/**
	 * 构造默认的远端阶段命名
	 */
	private RemotePhaseItem() {
		super();
	}

	/**
	 * 构造远端阶段命名，指定阶段命名和站点地址
	 * @param phase 阶段命名
	 * @param node 站点地址
	 */
	public RemotePhaseItem(Phase phase, Node node) {
		this();
		setPhase(phase);
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析远端阶段命名参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RemotePhaseItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出远端阶段命名
	 * @param reader 标记化读取器
	 */
	public RemotePhaseItem(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置阶段命名
	 * @param e Phase实例
	 */
	public void setPhase(Phase e) {
		Laxkit.nullabled(e);

		phase = e;
	}

	/**
	 * 返回阶段命名
	 * @return Phase实例
	 */
	public Phase getPhase() {
		return phase;
	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 返回当前远端阶段命名的数据副本
	 * @return 新的RemotePhaseItem实例
	 */
	public RemotePhaseItem duplicate() {
		return new RemotePhaseItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != RemotePhaseItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((RemotePhaseItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return phase.hashCode() ^ node.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("<%s>/%s", phase, node);
	}

	/*
	 * 比较两个远端阶段命名的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RemotePhaseItem that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(phase, that.phase);
		if (ret == 0) {
			ret = Laxkit.compareTo(node, that.node);
		}
		return ret;
	}


}