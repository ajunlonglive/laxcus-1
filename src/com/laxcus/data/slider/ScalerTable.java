/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.slider;

import java.io.*;
import java.util.*;

import com.laxcus.access.index.zone.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 列码位统计表 <br>
 * 
 * 记录一台计算机上，某一列的码位范围。
 * 
 * @author scott.liang
 * @version 1.1 5/16/2015
 * @since laxcus 1.1
 */
public abstract class ScalerTable implements Cloneable, Serializable, Classable, Comparable<ScalerTable> {

	private static final long serialVersionUID = 3749993962346562687L;

	/** 类型定义，见ScaleType **/
	private byte family;

	/** 列空间 */
	private Dock dock;

	/**
	 * 构造默认的列码位范围统计表
	 */
	protected ScalerTable(byte family) {
		super();
		setFamily(family);
	}

	/**
	 * 根据传入的列码位范围统计表，生成它的数据副本
	 * @param that ScaleTable实例
	 */
	protected ScalerTable(ScalerTable that) {
		super();
		family = that.family;
		dock = that.dock;
	}

	/**
	 * 设置码位计算器类型
	 * @param who 码位计算器类型
	 */
	private void setFamily(byte who) {
		if (!ScaleType.isFamily(who)) {
			throw new IllegalValueException("illegal family:%d", who);
		}
		family = who;
	}

	/**
	 * 返回码位计算器类型
	 * @return 码位计算器类型
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 设置列空间
	 * @param e Dock实例
	 */
	public void setDock(Dock e) {
		dock = e;
	}

	/**
	 * 返回列空间
	 * @return Dock实例
	 */
	public Dock getDock() {
		return dock;
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 将类输出为字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从字节数组中解析参数
	 * @param b  字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
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
	public int compareTo(ScalerTable that) {
		return Laxkit.compareTo(dock, that.dock);
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != this.getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((ScalerTable) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return dock.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 类型定义
		writer.write(family);
		// 写列空间对象
		writer.writeObject(dock);
		// 调用子类接口，将子类信息写入可类化存储器
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 类型定义
		setFamily(reader.read());
		// 读列空间
		dock = new Dock(reader);
		// 从可类化读取器中解析子类信息
		resolveSuffix(reader);
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

	/**
	 * ScaleTable子类对象生成它的浅层数据副本
	 * @return ScaleTable实例
	 */
	public abstract ScalerTable duplicate();

	/**
	 * 键集合
	 * @return Range列
	 */
	public abstract List<Range> keys();

	/**
	 * 查找权重
	 * @param key
	 * @return 权重值
	 */
	public abstract java.lang.Integer find(Range key);

	/**
	 * 集合数目
	 * @return 集合数目
	 */
	public abstract int size();

	/**
	 * 输出当前索引域
	 * @return IndexZone数组
	 */
	public abstract IndexZone[] put();

	/**
	 * 保存一个码位和它的统计次数
	 * @param codePoint 码位
	 * @param count 统计次数
	 */
	public abstract void add(java.lang.Number codePoint, int count);

	/**
	 * 将子类参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析子类参数信息
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);

}
