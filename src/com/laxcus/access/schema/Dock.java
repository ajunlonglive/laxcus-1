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
 * 列空间。<br><br>
 * 
 * 列空间是对数据表中某一个列的描述，由表名和列编号组成。每个列空间在LAXCUS集群中是唯一的。<br>
 * 
 * @author scott.liang 
 * @version 1.1 9/3/2015
 * @since laxcus 1.0
 */
public final class Dock implements Serializable, Cloneable, Classable, Markable, Comparable<Dock> {

	private static final long serialVersionUID = 2581905647090233410L;

	/** 数据表名称，这个参数必须有效。**/
	private Space space;

	/** 列编号，默认0值，表示未定义。 */
	private short columnId;

	/**
	 * 将Dock参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(space);
		writer.writeShort(columnId);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中读取Dock参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		space = new Space(reader);
		columnId = reader.readShort();
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的列空间
	 */
	private Dock() {
		super();
		columnId = 0; // 默认不指定
	}

	/**
	 * 根据传入的列空间对象，生成一个它的副本
	 * @param that 列空间实例
	 */
	private Dock(Dock that) {
		this();
		space = that.space.duplicate();
		columnId = that.columnId;
	}

	/**
	 * 构造列空间，并且指定它的表名和列编号
	 * @param space 表名
	 * @param columnId 列编号
	 */
	public Dock(Space space, short columnId) {
		this();
		setSpace(space);
		setColumnId(columnId);
	}

	/**
	 * 构造构造列空间，指定表名名称，列编号默认是0
	 * @param space 表名
	 */
	public Dock(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 构造列空间，并且指定它的表名和列编号
	 * @param schema 数据库名称
	 * @param table 数据表名称
	 * @param columnId 列编号
	 */
	public Dock(String schema, String table, short columnId) {
		this(new Space(schema, table), columnId);
	}

	/**
	 * 从可类化数据读取器中解析列空间参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Dock(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出列空间
	 * @param reader 标记化读取器
	 */
	public Dock(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置表名。参数必须有效，如果是空值弹出空指针异常。
	 * @param e 表名
	 * @throws NullPointerException
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 返回列名
	 * @return Fame实例
	 */
	public Fame getSchema() {
		return space.getSchema();
	}

	/**
	 * 设置列编号
	 * @param id 列编号
	 */
	public void setColumnId(short id) {
		columnId = id;
	}

	/**
	 * 返回列编号
	 * @return 短整型列编号
	 */
	public short getColumnId() {
		return columnId;
	}

	/**
	 * 返回当前列空间实例的数据副本
	 * @return Dock实例
	 */
	public Dock duplicate() {
		return new Dock(this);
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Dock.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((Dock) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode() ^ columnId;
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
	 * 返回列空间的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%d", space, columnId);
	}

	/**
	 * 对两个列空间进行排序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Dock that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(space, that.space);
		if (ret == 0) {
			ret = Laxkit.compareTo(columnId, that.columnId);
		}
		return ret;
	}

	/**
	 * 生成数据流并且输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 解析数据流，并且返回解析的字节长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 解析的长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

}