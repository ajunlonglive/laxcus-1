/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.net;

import java.io.*;
import java.util.*;

import com.laxcus.util.classable.*;

/**
 * 节点地址集合。<br>
 * 为避免增加/删除时可能发生的并发异常，涉及读取的操作都用同步关键字锁定。
 * 
 * @author scott.liang
 * @version 1.1 3/11/2015
 * @since laxcus 1.0
 */
public final class SiteSet implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 6395613448904527666L;

	/** 循环调用下标，从0开始(平均循环调用)，到集合的最大值时复0 */
	private int iterateIndex;

	/** 站点地址集合 */
	private ArrayList<SiteHost> array = new ArrayList<SiteHost>();

	/**
	 * 根据传入的节点地址集合实例，生成它的数据副本
	 * @param that
	 */
	private SiteSet(SiteSet that) {
		super();
		this.iterateIndex = that.iterateIndex;
		this.array.addAll(that.array);
		this.trim();
	}

	/**
	 * 构造默认的节点地址集合
	 */
	public SiteSet() {
		super();
		this.iterateIndex = 0;
	}

	/**
	 * 构造节点地址集合，同时指定它的存储空间
	 * @param size 节点成员数目
	 */
	public SiteSet(int size) {
		this();
		// 最小限制是1个
		if (size < 1) {
			size = 1;
		}
		array.ensureCapacity(size);
	}

	/**
	 * 构造节点地址集合，同时保存一批节点地址
	 * @param hosts
	 */
	public SiteSet(SiteHost[] hosts) {
		this(hosts == null ? 0 : hosts.length);
		this.add(hosts);
	}

	/**
	 * 构造节点地址集合，同时保存一批节点地址
	 * @param hosts
	 */
	public SiteSet(Collection<SiteHost> hosts) {
		this(hosts.size());
		this.add(hosts);
	}

	/**
	 * 从可类化读取中解析节点地址集合
	 * @param reader
	 */
	public SiteSet(ClassReader reader) {
		this();
		this.resolve(reader);
	}
	
	/**
	 * 增加一个主机地址。不允许空指针，或者主机地址重叠的现象存在。
	 * @param host FIXP服务器地址
	 * @return 保存成功返回真，否则假
	 */
	public synchronized boolean add(SiteHost host) {
		if (host != null && !array.contains(host)) {
			return this.array.add(host);
		}
		return false;
	}

	/**
	 * 删除一个主机地址。
	 * @param host FIXP服务器地址
	 * @return 删除成功返回真，否则假
	 */
	public synchronized boolean remove(SiteHost host) {
		return this.array.remove(host);
	}

	/**
	 * 增加一组主机地址。每个主机地址都是唯一的，不允许重叠现象存在。
	 * @param hosts FIXP服务器地址集合
	 * @return 返回增加的成员数
	 */
	public int add(SiteHost[] hosts) {
		int size = array.size();
		for (int i = 0; hosts != null && i < hosts.length; i++) {
			this.add(hosts[i]);
		}
		return array.size() - size;
	}

	/**
	 * 增加一组主机地址
	 * @param list 主机地址列表
	 * @return 返回增加的成员数目
	 */
	public int add(Collection<SiteHost> list) {
		int size = array.size();
		for (SiteHost that : list) {
			this.add(that);
		}
		return array.size() - size;
	}

	/**
	 * 删除一组主机地址.返回删除的数量
	 * @param list 主机地址列表
	 * @return 返回删除的成员数目
	 */
	public int remove(Collection<SiteHost> list) {
		int size = array.size();
		for (SiteHost that : list) {
			this.remove(that);
		}
		return size - array.size();
	}

	/**
	 * 检查主机地址是否存在
	 * @param host 主机址
	 * @return 返回或者假
	 */
	public synchronized boolean exists(SiteHost host) {
		return array.contains(host);
	}

	/**
	 * 复制当前全部节点地址，并且返回
	 * @return 主机地址列表
	 */
	public synchronized List<SiteHost> list() {
		return new ArrayList<SiteHost>(this.array);
	}

	/**
	 * 返回节点主机地址数目
	 * @return 主机地址数目
	 */
	public int size() {
		return this.array.size();
	}

	/**
	 * 判断节点主机地址是否为空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return this.size() == 0;
	}

	/**
	 * 将数组空间调整为实际大小(删除多余的空间)
	 */
	public synchronized void trim() {
		this.array.trimToSize();
	}

	/**
	 * 复制并且返回主机地址数组
	 * @return 主机地址数组
	 */
	public synchronized SiteHost[] toArray() {
		int size = array.size();
		SiteHost[] s = new SiteHost[size];
		for (int i = 0; i < size; i++) {
			s[i] = array.get(i);
		}
		return s;
	}

	/**
	 * 循环依次调用每一个主机地址
	 * @return 主机地址
	 */
	public synchronized SiteHost next() {
		int size = array.size();
		if (size > 0) {
			if (iterateIndex >= size) iterateIndex = 0;
			return array.get(iterateIndex++);
		}
		return null;
	}

	/**
	 * 找到指定的主机位置开始，返回它的下一个主机地址。如果没有，返回NULL
	 * @param that 当前主机
	 * @return 下一个主机地址
	 */
	public synchronized SiteHost next(SiteHost that) {
		int size = array.size();
		for (int index = 0; index < size; index++) {
			if (array.get(index).compareTo(that) != 0) {
				continue;
			}
			if (index + 1 < size) {
				iterateIndex = index + 1;
				return array.get(iterateIndex);
			}
		}
		// 返回最前面的
		if (size > 0) {
			return array.get(iterateIndex = 0);
		}
		return null;
	}

	/**
	 * 根据当前地址集合，克隆它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new SiteSet(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeInt(this.iterateIndex);
		writer.writeInt(array.size());
		for (SiteHost host : array) {
			writer.writeObject(host);
		}
		return writer.size() - scale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		this.iterateIndex = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SiteHost host = new SiteHost(reader);
			array.add(host);
		}
		return reader.getSeek() - scale;
	}

}