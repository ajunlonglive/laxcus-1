/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 检索到的分布任务组件单元
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class SeekTaskItem implements Classable, Serializable, Cloneable, Comparable<SeekTaskItem> {

	private static final long serialVersionUID = -559637102864543636L;

	/** 节点地址 **/
	private Node node;

	/** 阶段命名 **/
	private Phase phase;

	/**
	 * 构造默认和私有的检索到的分布任务组件单元
	 */
	private SeekTaskItem() {
		super();
	}

	/**
	 * 生成检索到的分布任务组件单元的副本
	 * 
	 * @param that SeekTaskItem实例
	 */
	private SeekTaskItem(SeekTaskItem that) {
		this();
		node = that.node;
		phase = that.phase;
	}

	/**
	 * 构造检索到的分布任务组件单元，指定节点地址和阶段命名
	 * 
	 * @param node 节点地址
	 * @param root 阶段命名
	 */
	public SeekTaskItem(Node node, Phase root) {
		this();
		setSite(node);
		setPhase(root);
	}

	/**
	 * 从可类化数据读取器中解析检索到的分布任务组件单元
	 * @param reader 可类化数据读取器
	 */
	public SeekTaskItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址
	 * 
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回站点地址
	 * 
	 * @return Node实例
	 */
	public Node getSite() {
		return node;
	}

	/**
	 * 设置分布任务组件根命名
	 * 
	 * @param e Phase实例
	 */
	public void setPhase(Phase e) {
		Laxkit.nullabled(e);

		phase = e;
	}

	/**
	 * 返回分布任务组件根命名
	 * 
	 * @return Phase实例
	 */
	public Phase getPhase() {
		return phase;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SeekTaskItem实例
	 */
	public SeekTaskItem duplicate() {
		return new SeekTaskItem(this);
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
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SeekTaskItem that) {
		if (that == null) {
			return 1;
		}

		// 比较参数
		int ret = Laxkit.compareTo(node, that.node);
		if (ret == 0) {
			ret = Laxkit.compareTo(phase, that.phase);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(node);
		writer.writeObject(phase);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		node = new Node(reader);
		phase = new Phase(reader);
		return reader.getSeek() - seek;
	}

}