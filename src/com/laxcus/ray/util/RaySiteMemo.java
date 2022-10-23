/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.util;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 集群节点。
 * 记录一个节点上的注册用户。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public final class RaySiteMemo implements Classable, Cloneable, Serializable , Comparable<RaySiteMemo> {

	private static final long serialVersionUID = -7621564079617273406L;

	/** 节点地址 **/
	private Node node;

	/** 用户签名签名 **/
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 构造默认和私有集群节点
	 */
	private RaySiteMemo() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that SiteMemo实例
	 */
	private RaySiteMemo(RaySiteMemo that) {
		super();
		node = that.node;
		array.addAll(that.array);
	}

	/**
	 * 构造集群节点，指定节点地址
	 * @param node 节点地址
	 */
	public RaySiteMemo(Node node) {
		this();
		setNode(node);
	}

	/**
	 * 从可类化数据读取器解析集群节点参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RaySiteMemo(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置节点地址，不允许空值.
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回节点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 保存用户签名，不允许空指针
	 * @param e Siger实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Siger e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除用户签名
	 * @param e Siger实例
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Siger e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}
	
	/**
	 * 判断节点存在
	 * @param e Siger实例
	 * @return 返回真或者假
	 */
	public boolean contains(Siger e){
		Laxkit.nullabled(e);
		return array.contains(e);
	}

	/**
	 * 输出全部用户签名列表
	 * @return Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 统计成员数目
	 * @return 成员数目
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
	 * 生成成员副本
	 * @return SiteMemo实例副本
	 */
	public RaySiteMemo duplicate() {
		return new RaySiteMemo(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RaySiteMemo that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(node, that.node);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		writer.writeObject(node);
		writer.writeInt(array.size());
		for (Siger e : array) {
			writer.writeObject(e);
		}

		// 返回写入的数据长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();

		node = new Node(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			array.add(e);
		}

		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}
}