/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * ESTABLISH.SCAN阶段扫描单元 <br>
 * 包括数据表名和锁定标识。用于ScanSession中。
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public final class ScanMember implements Serializable, Cloneable, Classable, Comparable<ScanMember> {

	private static final long serialVersionUID = -341710055171582687L;

	/** 数据表名 **/
	private Space space;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
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
		space = new Space(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的ESTABLISH.SCAN阶段扫描单元
	 */
	private ScanMember() {
		super();
	}

	/**
	 * 根据传入的ESTABLISH.SCAN阶段扫描单元对象，生成一个它的副本
	 * @param that
	 */
	private ScanMember(ScanMember that) {
		this();
		space = that.space.duplicate();
	}

	/**
	 * 构造构造ESTABLISH.SCAN阶段扫描单元，指定数据表名，锁定标记默认是0
	 * @param space  数据表名
	 */
	public ScanMember(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 构造ESTABLISH.SCAN阶段扫描单元，并且指定它的数据表名和锁定标记
	 * @param schema 数据库名称
	 * @param table 数据表名称
	 * @param lock 锁定标记
	 */
	public ScanMember(String schema, String table) {
		this(new Space(schema, table));
	}

	/**
	 * 从可类化数据读取器中解析ESTABLISH.SCAN阶段扫描单元参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanMember(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名
	 * @param e 数据表名
	 * @throws NullPointerException
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 返回当前ESTABLISH.SCAN阶段扫描单元实例的副本
	 * @return ScanMember实例
	 */
	public ScanMember duplicate() {
		return new ScanMember(this);
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ScanMember.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((ScanMember) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode() ;
	}

	/**
	 * 根据当前实例克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回ESTABLISH.SCAN阶段扫描单元的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return space.toString();
	}

	/**
	 * 对两个ESTABLISH.SCAN阶段扫描单元进行排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ScanMember that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}

		return Laxkit.compareTo(space, that.space);
	}

}