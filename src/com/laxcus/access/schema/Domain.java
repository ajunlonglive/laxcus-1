/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/** 
 * 一个表在LAXCUS分布多集群中的配置。<br>
 * 
 * Domain的作用是：TOP建表分配HOME集群时，根据Domain参数向一个或者多个HOME集群分派建表任务(最少向一个HOME集群分派)。
 * 以达到在多个HOME集群下运行一个表的目的。<br><br>
 * 
 * 参数包括两种：<br>
 * <1> 指定建表的HOME集群数量 <br>
 * <2> 指定建表分派到的HOME节点主机地址 <br><br>
 *  
 * 两种参数互斥，二选一。首先检查HOME集群地址集，在无效情况下选择HOME集群数量，HOME集群数最少一个。<br>
 * 
 * @author scott.liang 
 * @version 1.1 5/2/2015
 * @since laxcus 1.0
 */
public final class Domain implements Classable, Markable, Serializable, Cloneable {

	private static final long serialVersionUID = 3952305947198800320L;

	/** HOME节点数 */
	private int sites;

	/** HOME站点地址  */
	private TreeSet<Node> array = new TreeSet<Node>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 节点数目
		writer.writeInt(sites);
		// HOME主机地址
		writer.writeInt(array.size());
		for (Node that : array) {
			writer.writeObject(that);
		}
		// 写入的字节长度
		return writer.size() - size;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 指字节点数目
		sites = reader.readInt();
		// 分配的HOME主机地址
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node that = new Node(reader);
			array.add(that);
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}
	
	/**
	 * 生成传入对象的一个数据副本
	 * @param that Domain实例
	 */
	private Domain(Domain that) {
		this();
		sites = that.sites;
		for (Node e : that.array) {
			array.add(e.duplicate());
		}
	}
	
	/**
	 * 构造一个默认的集群配置
	 */
	public Domain() {
		super();
		sites = 0;
	}
	
	/**
	 * 从可类化读取器中解析集群参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public Domain(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出集群参数
	 * @param reader 标记化读取器
	 */
	public Domain(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 构造集群配置，指定HOME站点数目
	 * @param sites HOME站点数目
	 */
	public Domain(int sites) {
		this();
		setSites(sites);
	}

	/**
	 * 指定HOME集群数目
	 * @param i 集群数目
	 */
	public void setSites(int i) {
		if (i < 0) {
			throw new IllegalArgumentException("illegal sites " + i);
		}
		sites = i;
	}

	/**
	 * 返回HOME集群数目
	 * @return 集群数目
	 */
	public int getSites() {
		return sites;
	}

	/**
	 * 保存HOME站点地址
	 * @param e HOME站点
	 * @return 返回真或者假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 删除HOME站点地址
	 * @param e HOME站点
	 * @return 返回真或者假
	 */
	public boolean remove(Node e) {
		Laxkit.nullabled(e);

		return array.remove(e);
	}
	
	/**
	 * 返回HOME节点地址集合
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(array);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 地址集合数
	 * @return 整型数
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 清除地址集合
	 */
	public void clear() {
		array.clear();
	}
	
	/**
	 * 根据当前实例产生它的数据副本
	 * @return Domain实例
	 */
	public Domain duplicate() {
		return new Domain(this);
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
		if (array.size() > 0) {
			StringBuilder bf = new StringBuilder();
			for (Node node : array) {
				if (bf.length() > 0) bf.append(',');
				bf.append(node.toString());
			}
			return bf.toString();
		} else {
			return String.format("%d Groups", sites);
		}
	}
}