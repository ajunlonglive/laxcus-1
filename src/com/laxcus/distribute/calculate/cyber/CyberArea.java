/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.cyber;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 网络数据区<br>
 * 
 * CyberArea是分散在网络上，基于同一个“模”值的网络数据域集合。 <br>
 * 
 * CONDUCT的DIFFUSE/CONVERGE分布计算，或者CONTACT的快速计算过程中，基于同一个模值(mod)的分布信息集合。<br>
 * 汇集多个节点上的数据分布元信息，形成分布信息标记  -> 分布信息汇总的映射关系。<br>
 * 
 * @author scott.liang
 * @version 1.1 03/18/2015
 * @since laxcus 1.0
 */
public final class CyberArea implements Serializable, Cloneable, Classable, Comparable<CyberArea> {

	private static final long serialVersionUID = -1754874706518771645L;

	/** 模值 **/
	private long mod;

	/** 分布数据集合 **/
	private TreeSet<CyberField> array = new TreeSet<CyberField>();

	/**
	 * 构造默认和私有的网络数据区
	 */
	private CyberArea() {
		super();
	}

	/**
	 * 根据传入的网络数据区，构造一个浅层数据副本
	 * @param that CyberArea实例
	 */
	private CyberArea(CyberArea that) {
		this();
		mod = that.mod;
		array.addAll(that.array);
	}

	/**
	 * 构造网络数据区，指定模值
	 * @param mod 模值
	 */
	public CyberArea(long mod) {
		this();
		setMod(mod);
	}

	/**
	 * 从可类化数据读取器中解析网络数据区
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CyberArea(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置模值
	 * @param id 模值
	 */
	public void setMod(long id) {
		mod = id;
	}

	/**
	 * 返回模值
	 * @return 模值
	 */
	public long getMod() {
		return mod;
	}

	/**
	 * 保存网络数据域
	 * @param e CyberField实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(CyberField e) {
		Laxkit.nullabled(e);

		if (e.getField().getMod() != mod) {
			throw new IllegalValueException("cannot be match:[%,%d]", mod, 
					e.getField().getMod());
		}
		return array.add(e);
	}

	/**
	 * 保存一项网络数据域
	 * @param flag CyberFlag实例
	 * @param field FluxField实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(CyberFlag flag, FluxField field) {
		return add(new CyberField(flag, field));
	}

	/**
	 * 保存一项网络数据域
	 * @param node 节点地址
	 * @param taskId 任务号
	 * @param field FluxField实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Node node, long taskId, FluxField field) {
		CyberFlag key = new CyberFlag(node, taskId);
		CyberField value = new CyberField(key, field);
		return add(value);
	}

	/**
	 * 保存一批网络数据域
	 * @param that CyberArea实例
	 * @return 返回新增加的成员数目
	 */
	public int addAll(CyberArea that) {
		if (mod != that.mod) {
			throw new IllegalValueException("illegal mod:[%d,%d]", mod, that.mod);
		}
		int size = array.size();
		for (CyberField field : that.array) {
			add(field);
		}
		return array.size() - size;
	}

	/**
	 * 返回当前全部网络数据域
	 * @return  CyberField列表
	 */
	public List<CyberField> list() {
		return new ArrayList<CyberField>(array);
	}

	/**
	 * 返回集合中的成员数目
	 * @return 成员数目
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
	 * 计算数据长度
	 * @return 数据总长度
	 */
	public long length() {
		long len = 0L;
		for (CyberField field : array) {
			len += field.length();
		}
		return len;
	}

	/**
	 * 生成当前实例的浅层数据副本
	 * @return CyberArea实例
	 */
	public CyberArea duplicate() {
		return new CyberArea(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CyberArea.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((CyberArea) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (mod >>> 32 ^ mod);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d#%d", mod, array.size());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CyberArea that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(mod, that.mod);
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
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeLong(mod);
		writer.writeInt(array.size());
		for (CyberField field : array) {
			writer.writeObject(field);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		mod = reader.readLong();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CyberField field = new CyberField(reader);
			array.add(field);
		}
		return reader.getSeek() - seek;
	}

}