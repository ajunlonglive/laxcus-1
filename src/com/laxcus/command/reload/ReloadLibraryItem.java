/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reload;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 重装加载动态链接库单元
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class ReloadLibraryItem implements Classable, Cloneable, Serializable, Comparable<ReloadLibraryItem> {

	private static final long serialVersionUID = 6380860552699821364L;

	/** 本地节点地址 **/
	private Node site;
	
	/** 成功或者否 **/
	private boolean successful;

	/** 节点链接库文件名 **/
	private ArrayList<String> array = new ArrayList<String>();

	/**
	 * 构造默认的被刷新处理单元
	 */
	protected ReloadLibraryItem () {
		super();
		successful = false;
	}

	/**
	 * 根据传入实例，生成重装加载动态链接库单元的数据副本
	 * @param that ReloadLibraryItem实例
	 */
	protected ReloadLibraryItem(ReloadLibraryItem that) {
		super();
		site = that.site;
		successful = that.successful;
		array.addAll(that.array);
	}

	/**
	 * 构造重装加载动态链接库单元，指定用户签名和CALL站点
	 * @param site CALL站点，允许空值
	 */
	public ReloadLibraryItem(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 构造重装加载动态链接库单元，指定用户签名和CALL站点
	 * @param site CALL站点，允许空值
	 */
	public ReloadLibraryItem(Node site, boolean successful) {
		this(site);
		setSuccessful(successful);
	}
	
	/**
	 * 从可类化数据读取器中重装加载动态链接库单元
	 * @param reader 可类化数据读取器
	 */
	public ReloadLibraryItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置本地节点地址，不允许空值
	 * @param e 本地节点地址
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		site = e;
	}

	/**
	 * 返回本地节点地址
	 * @return 站点地址
	 */
	public Node getSite() {
		return site;
	}
	
	/**
	 * 设置成功或者否
	 * @param b
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}
	
	/**
	 * 判断成功
	 * @return
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 保存链接库文件名
	 * @param e
	 */
	public void add(String e) {
		Laxkit.nullabled(e);
		array.add(e);
	}

	/**
	 * 保存一批链接库文件名
	 * @param a 链接库文件名数组
	 */
	public void addAll(Collection<String> a) {
		array.addAll(a);
	}

	/**
	 * 输出全部链接库文件名
	 * @return 链接库文件名数组
	 */
	public List<String> list() {
		return new ArrayList<String>(array);
	}

	/**
	 * 生成当前实例的数据副本
	 * @return ReloadLibraryItem实例
	 */
	public ReloadLibraryItem duplicate() {
		return new ReloadLibraryItem(this);
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((ReloadLibraryItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s", site);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ReloadLibraryItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数到可类化写入器
	 * @param writer 可类化数据写入器
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(site);
		writer.writeBoolean(successful);
		writer.writeInt(array.size());
		for (String e : array) {
			writer.writeString(e);
		}
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		site = reader.readInstance(Node.class);
		successful = reader.readBoolean();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			String e = reader.readString();
			array.add(e);
		}
	}
}