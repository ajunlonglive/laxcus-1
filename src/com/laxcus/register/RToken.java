/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 运行环境参量 <br><br>
 * 
 * 子类有变量和文件夹两种类型，变量同时又具有多种类型。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public abstract class RToken implements Classable, Comparable<RToken> {

	/** 属性 **/
	byte attribute;

	/** 单元名称 **/
	Naming name;

	/**
	 * 构造运行运行环境参量
	 */
	protected RToken() {
		super();
		attribute = 0; // 不定义
	}

	/**
	 * 生成运行运行环境参量副本
	 * @param that
	 */
	protected RToken(RToken that) {
		this();
		attribute = that.attribute;
		name = that.name;
	}
	
	/**
	 * 构造运行运行环境参量，指定属性
	 * @param attribute 属性
	 * @param type 类型 
	 */
	protected RToken(byte attribute) {
		this();
		setAttribute(attribute);
	}

	/**
	 * 返回参数类型，见RTokenAttribute定义
	 * @return 参数类型的字节描述
	 */
	public byte getAttribute() {
		return attribute;
	}
	
	/**
	 * 判断是参数
	 * @return 返回真或者假
	 */
	public boolean isParameter() {
		return RTokenAttribute.isParameter(attribute);
	}

	/**
	 * 判断是文件夹
	 * @return 返回真或者假
	 */
	public boolean isFolder() {
		return RTokenAttribute.isFolder(attribute);
	}

	/**
	 * 设置参数类型，见RTokenAttribute定义
	 * @param who 参数类型
	 */
	protected void setAttribute(byte who) {
		if (!RTokenAttribute.isAttribute(who)) {
			throw new IllegalValueException("illegal attribute: %d", who);
		}
		attribute = who;
	}

	/**
	 * 设置参数名称
	 * @param e 字符串
	 */
	public void setName(String e) {
		if (e == null || e.isEmpty()) {
			throw new NullPointerException();
		}
		name = new Naming(e);
	}

	/**
	 * 设置参数名称
	 * @param e 参数名称
	 */
	public void setName(Naming e) {
		Laxkit.nullabled(e);

		name = e;
	}

	/**
	 * 返回参数名称
	 * @return 命名实例
	 */
	public Naming getName() {
		return name;
	}

	/**
	 * 返回参数名称的文本描述
	 * @return 字符串
	 */
	public String getNameText() {
		return name.get();
	}

	/**
	 * 调用子类实现接口，生成一个它的副本
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
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较一致
		return compareTo((RToken) that) == 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return attribute ^ name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RToken that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(attribute, that.attribute);
		if (ret == 0) {
			ret = Laxkit.compareTo(name, that.name);
		}
		return ret;
	}

	/**
	 * 将数据类型，标题、数据值输出到可类化写入器
	 * @param writer 可类化写入器
	 * @return 返回写入的字节长度
	 */
	@Override
	public int build(ClassWriter w) {
		final int size = w.size();

		ClassWriter writer = new ClassWriter();
		// 属性
		writer.write(attribute);
		// 标题名称
		writer.writeObject(name);
		// 保存子类参数信息
		buildSuffix(writer);

		// 读取内容
		byte[] b = writer.effuse();
		w.writeInt(b.length);
		w.write(b);

		// 返回写入的数据长度
		return w.size() - size;
	}

	/**
	 * 从可类化读取器中解析数据类型、参数标题、数据值
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader r) {
		final int seek = r.getSeek();

		// 从可类化读取器中读取Command字节流
		int len = r.readInt();
		byte[] b = r.read(len);

		ClassReader reader = new ClassReader(b);
		// 属性
		setAttribute(reader.read());
		// 标题名称
		name = new Naming(reader);
		// 解析参数
		resolveSuffix(reader);

		// 返回解析长度
		return r.getSeek() - seek;
	}

	/**
	 * 子类生成自己实例的数据副本
	 * @return RToken子类实例
	 */
	public abstract RToken duplicate();

	/**
	 * 将子类的参数写入可类化写入器
	 * @param writer 可类化写入器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类的参数
	 * @param reader 可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}