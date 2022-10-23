/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.disk.*;
import com.laxcus.site.*;

/**
 * 打印站点检测目录单元
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public final class CheckSitePathItem implements Serializable, Cloneable, Classable, Comparable<CheckSitePathItem> {

	private static final long serialVersionUID = 3022051569221720897L;

	/** 操作系统类型 **/
	private String os;

	/** 站点地址 **/
	private Node site;
	
	/** 磁盘参数 **/
	private TreeSet<PathTab> paths = new TreeSet<PathTab>();
	
	/**
	 * 构造默认和私有的打印站点检测目录单元
	 */
	private CheckSitePathItem() {
		super();
	}

	/**
	 * 生成打印站点检测目录单元数据副本
	 * @param that 原本
	 */
	private CheckSitePathItem(CheckSitePathItem that) {
		this();
		os = that.os;
		site = that.site;
		paths.addAll(that.paths);
	}

	/**
	 * 构造打印站点检测目录单元，指定站点地址
	 * @param node 站点地址
	 */
	public CheckSitePathItem(Node node) {
		this();
		setSite(node);
	}

	/**
	 * 从可类化数据读取器解析打印站点检测目录单元
	 * @param reader 可类化数据读取器
	 */
	public CheckSitePathItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 判断成功
	 * @return
	 */
	public boolean isSuccessful() {
		return os != null;
	}

	/**
	 * 设置为操作系统名
	 * @param name 系统名称
	 */
	public void setOS(String name) {
		os = name;
	}

	/**
	 * 返回操作系统名
	 * @return 字符串
	 */
	public String getOS() {
		return os;
	}
	
	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 返回检测目录
	 * @return 站点列表
	 */
	public List<PathTab> list() {
		return new ArrayList<PathTab>(paths);
	}

	/**
	 * 保存一个检测目录，不允许空指针
	 * @param e PathCard实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(PathTab e) {
		Laxkit.nullabled(e);

		return paths.add(e);
	}
	
	/**
	 * 保存一批检测目录
	 * @param a PathCard集合
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<PathTab> a) {
		int size = paths.size();
		for (PathTab e : a) {
			add(e);
		}
		return paths.size() - size;
	}
	
	/**
	 * 返回站点数目
	 * @return 站点数目
	 */
	public int size() {
		return paths.size();
	}

	/**
	 * 判断站点是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 产生数据副本
	 * @return CheckSitePathItem实例
	 */
	public CheckSitePathItem duplicate() {
		return new CheckSitePathItem(this);
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
		return String.format("%s / %s # %d", site, (os != null ? os : "null!"), paths.size());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CheckSitePathItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((CheckSitePathItem) that) == 0;
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
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CheckSitePathItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 操作系统
		writer.writeString(os);
		// 站点地址
		writer.writeObject(site);
		// 保存检测目录
		writer.writeInt(paths.size());
		for (PathTab e : paths) {
			writer.writeObject(e);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 操作系统
		os = reader.readString();
		// 站点地址
		site = new Node(reader);
		// 解析检测目录
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			PathTab e = new PathTab(reader);
			paths.add(e);
		}
		return reader.getSeek() - seek;
	}

}