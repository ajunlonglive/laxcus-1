/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.io.*;
import java.util.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户日志检索单元
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 */
public class ScanUserLogItem implements Classable, Cloneable, Serializable, Comparable<ScanUserLogItem> {

	private static final long serialVersionUID = 6380860552699821364L;

	/** 用户签名 **/
	private Siger siger;

	/** CALL站点地址 **/
	private Node site;
	
	/** 用户日志 **/
	private ArrayList<EchoLog> array = new ArrayList<EchoLog>();

	/**
	 * 构造默认的被刷新处理单元
	 */
	protected ScanUserLogItem () {
		super();
	}

	/**
	 * 根据传入实例，生成用户日志检索单元的数据副本
	 * @param that ScanUserLogItem实例
	 */
	protected ScanUserLogItem(ScanUserLogItem that) {
		super();
		siger = that.siger;
		site = that.site;
		array.addAll(that.array);
	}

	/**
	 * 构造用户日志检索单元，指定用户签名
	 * @param siger 用户签名
	 */
	public ScanUserLogItem(Siger siger) {
		this();
		setSiger(siger);
	}
	
	/**
	 * 构造用户日志检索单元，指定用户签名和CALL站点
	 * @param siger 用户签名
	 * @param site CALL站点，允许空值
	 */
	public ScanUserLogItem(Siger siger, Node site) {
		this(siger);
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中用户日志检索单元
	 * @param reader 可类化数据读取器
	 */
	public ScanUserLogItem (ClassReader reader) {
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
	 * 设置CALL站点地址，允许空值
	 * @param e CALL站点地址
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		site = e;
	}

	/**
	 * 返回CALL站点地址
	 * @return 站点地址
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 保存日志
	 * @param e
	 */
	public void add(EchoLog e) {
		Laxkit.nullabled(e);
		array.add(e);
	}
	
	/**
	 * 保存一批日志
	 * @param a 日志数组
	 */
	public void addAll(Collection<EchoLog> a) {
		array.addAll(a);
	}

	/**
	 * 输出全部日志
	 * @return
	 */
	public List<EchoLog> list() {
		return new ArrayList<EchoLog>(array);
	}

	/**
	 * 生成当前实例的数据副本
	 * @return ScanUserLogItem实例
	 */
	public ScanUserLogItem duplicate() {
		return new ScanUserLogItem(this);
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
		return compareTo((ScanUserLogItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ site.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (site != null) {
			return String.format("%s#%s", siger, site);
		}
		return String.format("%s", siger);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ScanUserLogItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(site, that.site);
		}
		return ret;
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
		writer.writeInstance(site);
		writer.writeInt(array.size());
		for (EchoLog e : array) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
		site = reader.readInstance(Node.class);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			EchoLog e = new EchoLog(reader);
			array.add(e);
		}
	}
}