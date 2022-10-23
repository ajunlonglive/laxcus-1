/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 缓存映像块集合<br><br>
 * 
 * 每个DATA站点下的不同数据表名的缓存映像块集合记录。
 * 
 * @author scott.liang
 * @version 1.1 10/13/2015
 * @since laxcus 1.0
 */
public final class CacheReflexStub implements Classable, Cloneable, Comparable<CacheReflexStub>, Serializable {

	private static final long serialVersionUID = -206242084633477440L;

	/** 表基点 **/
	private Spot spot;

	/** 数据块编号集合 **/
	private TreeSet<Long> array = new TreeSet<Long>();

	/**
	 * 构造默认的缓存映像块集合
	 */
	private CacheReflexStub() {
		super();
	}

	/**
	 * 根据传入的缓存映像块集合，生成它的浅层数据副本
	 * @param that CacheReflexStub实例
	 */
	private CacheReflexStub(CacheReflexStub that) {
		this();
		spot = that.spot.duplicate();
		array.addAll(that.array);
	}

	/**
	 * 构造缓存映像块集合，指定数据表名
	 * @param site 命令源头
	 * @param space 数据表名名称
	 */
	public CacheReflexStub(Node site, Space space) {
		this();
		spot = new Spot(site, space);
	}

	/**
	 * 从可类化数据读取器中解析缓存映像块集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CacheReflexStub(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 返回表基点
	 * @return Spot实例
	 */
	public Spot getSpot() {
		return spot;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return spot.getSpace();
	}

	/**
	 * 返回命令源头地址
	 * @return Node实例
	 */
	public Node getSite() {
		return spot.getSite();
	}

	/**
	 * 保存一个数据块编号
	 * @param stub 数据块编号
	 * @return 保存成功返回真，否则假
	 */
	public boolean addStub(long stub) {
		return array.add(stub);
	}

	/**
	 * 返回数据块编号集合
	 * @return List<Long>
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>(array);
	}

	/**
	 * 返回数据块编号数目
	 * @return 数据块编号数目
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
	 * 判断数据表名和数据块编号匹配
	 * @param space  数据表名
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	public boolean contains(Space space, long stub) {
		boolean success = (spot.getSpace().compareTo(space) == 0);
		if (success) {
			success = array.contains(stub);
		}
		return success;
	}

	/**
	 * 返回当前实例的数据副本
	 * @return CacheReflexStub实例
	 */
	public CacheReflexStub duplicate() {
		return new CacheReflexStub(this);
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
		if (that == null || that.getClass() != CacheReflexStub.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((CacheReflexStub) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return spot.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CacheReflexStub that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(spot, that.spot);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 数据表名站点
		writer.writeObject(spot);
		// 数据块编号
		writer.writeInt(array.size());
		for (long stub : array) {
			writer.writeLong(stub);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 数据表名站点
		spot = new Spot(reader);
		// 数据块编号
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(stub);
		}
		return reader.getSeek() - seek;
	}

}