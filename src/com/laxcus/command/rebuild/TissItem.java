/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.Node;

/**
 * RUSH/COMPACT命令执行结果单元。<br>
 * 
 * @author scott.liang
 * @version 1.1 8/12/2016
 * @since laxcus 1.0
 */
public final class TissItem implements Serializable,Classable, Cloneable, Comparable<TissItem> {

	private static final long serialVersionUID = -5132649461132731233L;

	/** 节点地址  **/
	private Node site;

	/** RUSH/COMPACT执行结果代码(JNI返回码） **/
	private int state;

	/**
	 * 构造默认和私有的RUSH/COMPACT命令执行结果单元
	 */
	private TissItem() {
		super();
	}

	/**
	 * 根据传入的RUSH/COMPACT命令执行结果单元，生成它的数据副本
	 * @param that TissItem实例
	 */
	private TissItem(TissItem that) {
		this();
		site = that.site;
		state = that.state;
	}

	/**
	 * 构造RUSH/COMPACT命令执行结果单元，指定参数
	 * @param node 节点地址
	 * @param state RUSH执行结果代码(JNI返回码）
	 */
	public TissItem(Node node, int state) {
		this();
		setSite(node);
		setState(state);
	}

	/**
	 * 从可类化数据读取器中解析RUSH/COMPACT命令执行结果单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TissItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置节点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回节点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 设置RUSH执行结果代码(JNI返回码）
	 * @param e RUSH执行结果代码(JNI返回码）
	 */
	public void setState(int e) {
		state = e;
	}

	/**
	 * 返回RUSH执行结果代码(JNI返回码）
	 * @return RUSH执行结果代码(JNI返回码）
	 */
	public int getState() {
		return state;
	}

	/**
	 * 判断是成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return state >= 0;
	}

	/**
	 * 判断是失败
	 * @return 返回真或者假
	 */
	public boolean isFailed() {
		return !isSuccessful();
	}

	/**
	 * 生成TissItem的数据副本
	 * @return TissItem实例
	 */
	public TissItem duplicate() {
		return new TissItem(this);
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
		return String.format("%s/%d", site, state);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TissItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TissItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int seek = writer.size();
		writer.writeObject(site);
		writer.writeInt(state);
		return writer.size() - seek;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		site = new Node(reader);
		state = reader.readInt();
		return reader.getSeek() - seek;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TissItem that) {
		return Laxkit.compareTo(site, that.site);
	}

}