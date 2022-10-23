/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 表基点 <br>
 * 
 * 由站点地址和表空间组成，描述一个表所在的站点。
 * 
 * @author scott.liang
 * @version 1.3 8/12/2016
 * @since laxcus 1.0
 */
public final class Spot implements Serializable, Cloneable, Classable, Markable, Comparable<Spot> {

	private static final long serialVersionUID = -3372034335502656451L;

	/** 站点地址 */
	private Node site;

	/** 数据表空间 */
	private Space space;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(site);
		writer.writeObject(space);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		site = new Node(reader);
		space = new Space(reader);
		return reader.getSeek() - seek;
	}
	
	/**
	 * 根据传入对象生成它的数据副本
	 * @param that Spot实例
	 */
	private Spot(Spot that) {
		this();
		site = that.site.duplicate();
		space = that.space.duplicate();
	}

	/**
	 * 构造默认的表基点
	 */
	private Spot() {
		super();
	}

	/**
	 * 构造表基点，指定站点地址和数据表名
	 * @param site 站点
	 * @param space 表名
	 */
	public Spot(Node site, Space space) {
		this();
		setSite(site);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析表基点参数
	 * @param reader 可类化数据读取器
	 * @since 1.2
	 */
	public Spot(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出表基点
	 * @param reader 标记化读取器
	 */
	public Spot(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置站点地址
	 * @param e 站点实例
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
	 * 设置数据表名，不允许空指针
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 返回表基点的数据副本
	 * @return Spot实例
	 */
	public Spot duplicate() {
		return new Spot(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Spot.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Spot) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode() ^ space.hashCode();
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
		return String.format("%s#%s", site, space);
	}

	/*
	 * 比较两个表基点的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Spot that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(site, that.site);
		if (ret == 0) {
			ret = Laxkit.compareTo(space, that.space);
		}
		return ret;
	}


}