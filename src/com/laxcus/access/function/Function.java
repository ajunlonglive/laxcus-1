/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function;

import java.io.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 可编程、可定义函数
 * 
 * @author scott.liang
 * @version 1.1 12/27/2015
 * @since laxcus 1.0
 */
public abstract class Function implements Serializable, Cloneable, Markable, Classable {

	private static final long serialVersionUID = 5728482317070184256L;

	/** 函数原语描述，取自正则表达式的解析结果 **/
	private String primitive;

	/** 允许产生默认列。默认是不允许 **/
	private boolean supportDefault;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeString(primitive);
		writer.writeBoolean(supportDefault);
		buildSuffix(writer);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		primitive = reader.readString();
		supportDefault = reader.readBoolean();
		resolveSuffix(reader);
		// 返回解析长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个基础函数类
	 */
	protected Function() {
		super();
		// 不允许产生默认列
		supportDefault = false;
	}

	/**
	 * 根据传入基础函数参数，生成它的副本
	 * @param that 基础函数实例
	 */
	protected Function(Function that) {
		this();
		primitive = that.primitive;
		supportDefault = that.supportDefault;
	}

	/**
	 * 设置描述原语
	 * @param e 字符串
	 */
	protected void setPrimitive(String e) {
		primitive = e;
	}

	/**
	 * 返回描述原语
	 * @return 字符串
	 */
	public String getPrimitive() {
		return primitive;
	}

	/**
	 * 设置允许产生默认列
	 * @param b 默认产生
	 */
	public void setSupportDefault(boolean b) {
		supportDefault = b;
	}

	/**
	 * 判断允许产生默认列
	 * @return 返回真或者假
	 */
	public boolean isSupportDefault() {
		return supportDefault;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 由子类实现，生成当时函数实例的数据副本
	 * @return Function子类实例
	 */
	public abstract Function duplicate();

	/**
	 * 将子类私有的参数写入可类化存储器
	 * @param writer 可类化存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类私有的参数
	 * @param reader 可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);	

}