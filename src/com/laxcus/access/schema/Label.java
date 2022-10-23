/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.schema;

import java.io.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 表标签。<br>
 * 它是对数据表的列全部属性的描述，包括数据表名和列标记，区别于Dock定义。<br>
 * 
 * @author scott.liang
 * @version 1.1 2/12/2012
 * @since laxcus 1.0
 */
public final class Label implements Classable, Markable, Serializable, Cloneable, Comparable<Label> {

	private static final long serialVersionUID = 1627239912214998544L;

	/** 表空间 */
	private Space space;

	/** 列标记 **/
	private ColumnAttributeTag tag;

	/**
	 * 将表标签参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeObject(space);
		writer.writeObject(tag);
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析表标签参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		space = new Space(reader);
		tag = new ColumnAttributeTag(reader);
		return reader.getSeek() - scale;
	}

	/**
	 * 构造一个列标签
	 */
	private Label() {
		super();
	}

	/**
	 * 根据传入的标签参数，生成它的副本
	 * @param that Label实例
	 */
	private Label(Label that) {
		this();
		space = (Space) that.space.clone();
		tag = (ColumnAttributeTag) that.tag.clone();
	}

	/**
	 * 构造一个标签，并且设置它的表名和列基础参数
	 * @param space 表名
	 * @param tag 列标记
	 */
	public Label(Space space, ColumnAttributeTag tag) {
		this();
		setSpace(space);
		setTag(tag);
	}

	/**
	 * 从可类化读取器中解析表标签参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public Label(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出表标签参数
	 * @param reader 标记化读取器
	 */
	public Label(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置表名
	 * @param e 表名实例
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回表名
	 * @return 表名实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置列标记
	 * @param e 列标记实例
	 */
	public void setTag(ColumnAttributeTag e) {
		tag = e;
	}

	/**
	 * 返回列标记
	 * @return 列标记实例
	 */
	public ColumnAttributeTag getTag() {
		return tag;
	}

	/**
	 * 返回列编号
	 * @return 列编号的短整型描述
	 */
	public short getColumnId() {
		return tag.getColumnId();
	}

	/**
	 * 返回列的数据类型
	 * @return 列数据类型的字节描述
	 */
	public byte getFamily() {
		return tag.getType();
	}

	/**
	 * 返回列名称
	 * @return 字符串
	 */
	public String getName() {
		return tag.getName().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Label.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Label) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode() ^ tag.hashCode();
	}

	/**
	 * 使用当前对象实例克隆它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Label(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Label that) {
		if (that == null) {
			return 1;
		}
		int ret = space.compareTo(that.space);
		if (ret == 0) {
			ret = tag.compareTo(that.tag);
		}
		return ret;
	}

}