/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.util;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * WATCH成员记录。
 * 记录一个成员在集群的注册/登录站点的分布情况。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public final class MemberMemo implements Classable, Cloneable, Serializable , Comparable<MemberMemo> {

	private static final long serialVersionUID = 7384048522992062264L;

	/** 账号签名 **/
	private Siger siger;

	/** 注册的节点地址 **/
	private TreeSet<Node> sites = new TreeSet<Node>();

	/**
	 * 构造默认和私有集群成员
	 */
	private MemberMemo() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that MemberMemo实例
	 */
	private MemberMemo(MemberMemo that) {
		super();
		siger = that.siger;
		sites.addAll(that.sites);
	}

	/**
	 * 构造集群成员，指定账号签名
	 * @param siger 账号签名
	 */
	public MemberMemo(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器解析集群成员参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public MemberMemo(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号签名，不允许空值.
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回账号签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 加入一组记录
	 * @param that MemberMemo实例
	 * @return 加入成功返回真，否则假
	 */
	public boolean accede(MemberMemo that) {
		boolean success = (siger.compareTo(that.siger) == 0);
		if (success) {
			sites.addAll(that.sites);
		}
		return success;
	}

	/**
	 * 保存节点地址，不允许空指针
	 * @param e Node实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node e) {
		if (e != null) {
			return sites.add(e);
		}
		return false;
	}
	
	/**
	 * 删除节点地址
	 * @param e Node实例
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node e) {
		Laxkit.nullabled(e);
		return sites.remove(e);
	}
	
	/**
	 * 判断节点存在
	 * @param e Node实例
	 * @return 返回真或者假
	 */
	public boolean contains(Node e){
		Laxkit.nullabled(e);
		return sites.contains(e);
	}

	/**
	 * 输出全部节点地址列表
	 * @return Node列表
	 */
	public List<Node> list() {
		return new ArrayList<Node>(sites);
	}

	/**
	 * 统计成员数目
	 * @return 成员数目
	 */
	public int size() {
		return sites.size();
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
	 * @return MemberMemo实例副本
	 */
	public MemberMemo duplicate() {
		return new MemberMemo(this);
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
	public int compareTo(MemberMemo that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(siger, that.siger);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();

		writer.writeObject(siger);
		writer.writeInt(sites.size());
		for (Node e : sites) {
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

		siger = new Siger(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Node e = new Node(reader);
			sites.add(e);
		}

		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}
}