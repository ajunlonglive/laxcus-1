/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 云应用包单元
 * 
 * @author scott.liang
 * @version 1.1 02/08/2020
 * @since laxcus 1.0
 */
public class CloudWareItem implements Classable, Serializable, Cloneable, Comparable<CloudWareItem> {

	private static final long serialVersionUID = 1912534794294507850L;

	/** 云应用单元地址 **/
	private Node site;

	/** 云应用成员 **/
	private ArrayList<CloudWareElement> array = new ArrayList<CloudWareElement>();

	/**
	 * 构造默认和私有的云应用包单元
	 */
	private CloudWareItem() {
		super();
	}

	/**
	 * 生成云应用包单元的数据副本
	 * @param that CloudWareItem实例
	 */
	private CloudWareItem(CloudWareItem that) {
		this();
		site = that.site;
		array.addAll(that.array);
	}

	/**
	 * 构造云应用包单元，指定参数
	 * @param site 站点地址
	 */
	public CloudWareItem(Node site) {
		this();
		setSite(site);
	}

	/**
	 * 从可类化数据读取器中解析云应用包单元
	 * @param reader 可类化数据读取器
	 */
	public CloudWareItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个成员
	 * @param e CloudWareElement成员
	 * @return
	 */
	public boolean add(CloudWareElement e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 保存一组成员
	 * @param a CloudWareElement集合
	 * @return
	 */
	public int addAll(Collection<CloudWareElement> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 输出全部成员
	 * @return CloudWareElement列表
	 */
	public List<CloudWareElement> list() {
		return new ArrayList<CloudWareElement>(array);
	}
	
	/**
	 * 返回云应用成员数
	 * 
	 * @return 成员数
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 设置单元地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}
	
	/**
	 * 返回单元地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return CloudWareItem实例
	 */
	public CloudWareItem duplicate() {
		return new CloudWareItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CloudWareItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((CloudWareItem) that) == 0;
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
	public int compareTo(CloudWareItem that) {
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
		// 节点地址
		writer.writeObject(site);
		// 云应用成员
		writer.writeInt(array.size());
		for (CloudWareElement e : array) {
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
		// 节点
		site = new Node(reader);
		// 云成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CloudWareElement e = new CloudWareElement(reader);
			array.add(e);
		}
		return reader.getSeek() - seek;
	}

}