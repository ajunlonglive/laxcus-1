/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 布尔值参数
 * 
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RBoolean extends RParameter {

	/** 逻辑参数 **/
	private boolean value;

	/**
	 * 根据传入的布尔值参数，生成它的副本
	 * @param that
	 */
	private RBoolean(RBoolean that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的布尔值参数
	 */
	public RBoolean() {
		super(RParameterType.BOOLEAN);
	}

	/**
	 * 建立一个布尔值参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RBoolean(String name, boolean value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 建立一个布尔值参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RBoolean(Naming name, boolean value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 从可类化读取器中解析布尔值
	 * @param reader 可类化读取器
	 */
	public RBoolean(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置布尔参数
	 * @param b 布尔参数
	 */
	public void setValue(boolean b) {
		value = b;
	}

	/**
	 * 返回布尔参数
	 * @return 布尔参数
	 */
	public boolean getValue() {
		return value;
	}

	/**
	 * 判断是真
	 * @return 是真
	 */
	public boolean isTrue() {
		return value;
	}

	/**
	 * 判断是假
	 * @return 是假
	 */
	public boolean isFalse() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.RToken#duplicate()
	 */
	@Override
	public RBoolean duplicate() {
		return new RBoolean(this);
	}

	/**
	 * 将布尔参数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeBoolean(value);
	}

	/**
	 * 从可类化读取器中解析布尔参数
	 * @see com.laxcus.distribute.parameter.RToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		value = reader.readBoolean();
	}
	
}