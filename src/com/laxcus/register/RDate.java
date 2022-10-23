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
 * 日期参数
 * 
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RDate extends RParameter {

	/** 日期参数 **/
	private int value;

	/**
	 * 根据传入的日期参数，生成它的副本
	 * @param that
	 */
	private RDate(RDate that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的日期参数
	 */
	public RDate() {
		super(RParameterType.DATE);
	}

	/**
	 * 建立一个默认的日期参数，同时指定名称和参数
	 * @param name 名称
	 * @param value 参数值
	 */
	public RDate(String name, int value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个默认的日期参数，同时指定名称和参数
	 * @param name 名称
	 * @param value 参数值
	 */
	public RDate(Naming name, int value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析日期参数
	 * @param reader 可类化读取器
	 */
	public RDate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置参数值
	 * @param i 参数值
	 */
	public void setValue(int i) {
		value = i;
	}

	/**
	 * 返回参数值
	 * @return 参数值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.RParameter#duplicate()
	 */
	@Override
	public RDate duplicate() {
		return new RDate(this);
	}

	/**
	 * 将日期参数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析日期参数
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		value = reader.readInt();
	}

}