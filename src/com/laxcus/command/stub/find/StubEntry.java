/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.site.Node;

/**
 * 数据块实体。<br>
 * 
 * 记录一个DATA站点下的部分数据块编号
 * 
 * @author scott.liang
 * @version 1.1 12/7/2013
 * @since laxcus 1.0
 */
public final class StubEntry implements Serializable, Cloneable, Classable, Comparable<StubEntry> {
	
	private static final long serialVersionUID = 3421254588051000510L;

	/** DATA站点地址 **/
	private Node node;
	
	/** 数据块编号集合 **/
	private TreeSet<Long> array = new TreeSet<Long>();

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();

		writer.writeObject(node);
		writer.writeInt(array.size());
		for (long stub : array) {
			writer.writeLong(stub);
		}

		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		node = new Node(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(stub);
		}
		return reader.getSeek() - scale;
	}
	
	/**
	 * 构造默认的数据块实体
	 */
	public StubEntry() {
		super();
	}

	/**
	 * 构造数据块实体，指定地址
	 * @param node
	 */
	public StubEntry(Node node) {
		this();
		this.setNode(node);
	}

	/**
	 * 根据当前实例，生成它的数据副本
	 * @param that
	 */
	private StubEntry(StubEntry that) {
		super();
		node = that.node;
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中解析数据块实体
	 * @param reader - 可类化数据读取器
	 * @since 1.1 
	 */
	public StubEntry(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		this.node = e;
	}
	
	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return this.node;
	}
	
	/**
	 * 保存一个数据块编号
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	public boolean add(long stub) {
		return array.add(stub);
	}
	
	/**
	 * 保存一组数据块编号
	 * @param a 数据块编号
	 * @return
	 */
	public int addAll(Collection<Long> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}
	
	/**
	 * 判断数据块编号存在
	 * @param stub 数据块编号
	 * @return 存在返回“真”，否则“假”。
	 */
	public boolean contains(long stub) {
		return array.contains(stub);
	}
	
	/**
	 * 输出全部数据块编号
	 * @return 数据块编号列表
	 */
	public List<Long> list() {
		return new ArrayList<Long>(array);
	}

	/**
	 * 统计数据块编号数目
	 * @return 数据块编号数目
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

	/**
	 * 生成当前实例的数据副本
	 * @return 返回StubEntry实例
	 */
	public StubEntry duplicate() {
		return new StubEntry(this);
	}
	
	/**
	 * 克隆当前实例的数据副本
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
	public int compareTo(StubEntry that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(node, that.node);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubEntry.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubEntry) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode();
	}

}