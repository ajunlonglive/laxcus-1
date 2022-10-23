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
 * 字符串变量
 * 
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RString extends RParameter {

	/** 字符串变量 */
	private String value;

	/**
	 * 根据传入的字符串值参数，生成它的副本
	 * @param that RString实例
	 */
	private RString(RString that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的字符串参数
	 */
	protected RString() {
		super(RParameterType.STRING);
	}

	/**
	 * 建立一个字符串参数，同时指定它的名称和数值
	 * @param name 参数名称
	 * @param value 字符串
	 */
	public RString(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个字符串实例，同时指定它的标题和参数
	 * @param name 参数名称
	 * @param value 字符串
	 */
	public RString(Naming name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析字符串
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RString(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置字符串值
	 * @param e 字符串值
	 */
	public void setValue(String e) {
		Laxkit.nullabled(e);

		value = e;
	}

	/**
	 * 返回字符串值
	 * @return 字符串值
	 */
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.RParameter#duplicate()
	 */
	@Override
	public RString duplicate() {
		return new RString(this);
	}

	/*
	 * 将字符串写入可类化写入器
	 * @see com.laxcus.distribute.value.RParameter#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer); writer.writeString(value);
	}

	/*
	 * 从可类化读取器中读取字节串
	 * @see com.laxcus.distribute.value.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader); value = reader.readString();
	}
	

}