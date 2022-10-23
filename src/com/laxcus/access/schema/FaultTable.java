/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 表基点 <br>
 * 
 * 由用户签名和表空间组成。存在于磁盘上，因为某些故障不能使用。
 * 
 * @author scott.liang
 * @version 1.0 6/26/2019
 * @since laxcus 1.0
 */
public final class FaultTable implements Serializable, Cloneable, Classable, Markable, Comparable<FaultTable> {

	private static final long serialVersionUID = 5977513927440025215L;

	/** 用户签名 */
	private Siger siger;

	/** 数据表空间 */
	private Space space;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(siger);
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
		siger = new Siger(reader);
		space = new Space(reader);
		return reader.getSeek() - seek;
	}
	
	/**
	 * 根据传入对象生成它的数据副本
	 * @param that FaultTable实例
	 */
	private FaultTable(FaultTable that) {
		this();
		siger = that.siger.duplicate();
		space = that.space.duplicate();
	}

	/**
	 * 构造默认的表基点
	 */
	private FaultTable() {
		super();
	}

	/**
	 * 构造表基点，指定用户签名和数据表名
	 * @param sigers 用户签名
	 * @param space 表名
	 */
	public FaultTable(Siger sigers, Space space) {
		this();
		setSiger(sigers);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析表基点参数
	 * @param reader 可类化数据读取器
	 */
	public FaultTable(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出表基点
	 * @param reader 标记化读取器
	 */
	public FaultTable(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置用户签名
	 * @param e 用户签名实例
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
	 * @return FaultTable实例
	 */
	public FaultTable duplicate() {
		return new FaultTable(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FaultTable.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FaultTable) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ space.hashCode();
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
		return String.format("%s#%s", siger, space);
	}

	/*
	 * 比较两个表基点的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FaultTable that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(space, that.space);
		}
		return ret;
	}


}