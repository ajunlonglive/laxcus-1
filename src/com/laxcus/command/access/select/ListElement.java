/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.select;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * SELECT检索结果在终端/应用接口上的显示成员，包括数据表的列和SQL函数。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/18/2015
 * @since laxcus 1.0
 */
public abstract class ListElement implements Serializable, Cloneable, Classable, Comparable<ListElement> {

	private static final long serialVersionUID = 4020687106284903367L;

	/** 显示成员类型，列或者函数 **/
	public final static byte COLUMN = 1;
	public final static byte FUNCTION = 2;

	/** 显示成员类型(列成员或者函数成员) */
	private byte type;

	/** 列的表名(因为可能存在多个表混合显示的现象，所以这里需要指定列成员和函数成员的表名，如JOIN操作) **/
	private Space space;

	/** 别名(用户临时定义名称) */
	private Naming alias;

	/**
	 * 构造默认的显示成员
	 */
	private ListElement() {
		super();
	}

	/**
	 * 构造显示成员，并且指定它的显示属性
	 * @param type
	 */
	protected ListElement(byte type) {
		this();
		setType(type);
	}

	/**
	 * 根据传入参数构造它的副本
	 * @param that ListElement实例
	 */
	protected ListElement(ListElement that) {
		this();
		type = that.type;
		if (that.space != null) {
			space = that.space.duplicate();
		}
		if (that.alias != null) {
			alias = that.alias.duplicate();
		}
	}

	/**
	 * 设置数据表名称
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名称
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置成员属性
	 * @param who 成员属性
	 */
	private void setType(byte who) {
		switch (who) {
		case ListElement.COLUMN:
		case ListElement.FUNCTION:
			type = who;
			break;
		default:
			throw new IllegalValueException("illegal element %d", who);
		}
	}

	/**
	 * 返回成员属性
	 * @return 成员属性
	 */
	public byte getType() {
		return type;
	}

	/**
	 * 判断是否属于列成员
	 * @return 返回真或者假
	 */
	public boolean isColumn() {
		return type == ListElement.COLUMN;
	}

	/**
	 * 判断是否属于函数成员
	 * @return 返回真或者假
	 */
	public boolean isFunction() {
		return type == ListElement.FUNCTION;
	}

	/**
	 * 设置别名
	 * @param e 别名字符串
	 */
	public void setAlias(String e) {
		if (e == null) {
			alias = null;
		} else {
			alias = new Naming(e);
		}
	}

	/**
	 * 设置别名
	 * @param e 别名命名
	 */
	public void setAlias(Naming e) {
		alias = e;
	}

	/**
	 * 返回别名
	 * @return 别名字符串
	 */
	public String getAlias() {
		if (alias == null) {
			return null;
		}
		return alias.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();

		writer.write(type);
		writer.writeObject(space);
		writer.writeInstance(alias);
		
		// 子类参数
		buildSuffix(writer);

		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		type = reader.read();
		space = new Space(reader);
		alias = reader.readInstance(Naming.class);

		// 子类参数
		resolveSuffix(reader);

		return reader.getSeek() - seek;
	}

	/**
	 * 调用子类实例，克隆一个它的数据副本
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

		return compareTo((ListElement) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return type ^ space.hashCode() ^ getIdentity();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ListElement that) {
		// 空对象排在前面，有效对象排在后面
		if (that == null) {
			return 1;
		} else if (space == null) {
			return -1; 
		}

		int ret = Laxkit.compareTo(space, that.space);
		if (ret == 0) {
			ret = Laxkit.compareTo(type, that.type);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(getIdentity(), that.getIdentity());
		}
		return ret;
	}

	/**
	 * 子类对象生成自己的数据副本
	 * @return ListElement子类实例
	 */
	public abstract ListElement duplicate();

	/**
	 * 返回实际的列编号(对应Table中的属性ID号)
	 * @return 固定列编号
	 */
	public abstract short getColumnId();

	/**
	 * 根据显示成员类型，返回对应的编号。<br>
	 * 如果是SQL函数成员，返回它的函数编号；如果是列成员，返回列编号。<br>
	 * 函数编号是在所有列编号之外的临时标记。<br>
	 * @return 当前编号
	 */
	public abstract short getIdentity();

	/** 
	 * 返回成员参数的名称。<br>
	 * 如果是SQL函数成员，返回它的别名或者指定函数名。<br>
	 * 如果是列成员，返回它的别名或者列实际名称。<br>
	 * @return 成员参数名称
	 */
	public abstract String getName();

	/**
	 * 如果是列成员，返回它的列属性；如果是函数成员，返回它的计算结果列的属性。
	 * @return 计算结果列属性
	 */
	public abstract byte getFamily();

	/**
	 * 将显示成员参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析显示成员参数信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}