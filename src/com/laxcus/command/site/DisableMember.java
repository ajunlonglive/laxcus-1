/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 失效成员
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 */
public class DisableMember implements Classable, Cloneable, Serializable, Comparable<DisableMember> {

	private static final long serialVersionUID = 6380860552699821364L;

	/** 用户签名 **/
	private Siger siger;

	/** 用户数据表 **/
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 构造默认的被刷新处理单元
	 */
	private DisableMember() {
		super();
	}

	/**
	 * 根据传入实例，生成失效成员的数据副本
	 * @param that DisableMember实例
	 */
	private DisableMember(DisableMember that) {
		super();
		siger = that.siger;
		array.addAll(that.array);
	}

	/**
	 * 构造失效成员，指定用户签名
	 * @param siger 用户签名
	 */
	public DisableMember(Siger siger) {
		this();
		setSiger(siger);
	}
	
	/**
	 * 从可类化数据读取器中失效成员
	 * @param reader 可类化数据读取器
	 */
	public DisableMember (ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 保存数据表
	 * @param e
	 */
	public void add(Space e) {
		Laxkit.nullabled(e);
		array.add(e);
	}
	
	/**
	 * 保存一批数据表
	 * @param a 数据表数组
	 */
	public void addAll(Collection<Space> a) {
		array.addAll(a);
	}

	/**
	 * 输出全部数据表
	 * @return Space列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 生成当前实例的数据副本
	 * @return DisableMember实例
	 */
	public DisableMember duplicate() {
		return new DisableMember(this);
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
		if (that == null || this.getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((DisableMember ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s", siger);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DisableMember that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(siger, that.siger);
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
		writer.writeObject(siger);
		writer.writeInt(array.size());
		for (Space e : array) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			array.add(e);
		}
	}
}