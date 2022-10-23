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
 * 时间型参数
 *
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RTime extends RParameter {

	/** 时间型变量 **/
	private int value;

	/**
	 * 根据传入的时间型值参数，生成它的副本
	 * @param that
	 */
	private RTime(RTime that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的时间型参数
	 */
	public RTime() {
		super(RParameterType.TIME);
	}

	/**
	 * 建立一个时间型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RTime(String name, int value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个时间型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RTime(Naming name, int value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析时间参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public RTime(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置时间型参数
	 * @param i 时间整型值
	 */
	public void setValue(int i) {
		value = i;
	}

	/**
	 * 返回时间型参数
	 * @return 整型值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.RParameter#duplicate()
	 */
	@Override
	public RParameter duplicate() {
		return new RTime(this);
	}

	/**
	 * 将时间参数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer); writer.writeInt(value);
	}

	/**
	 * 从可类化读取器读取时间参数
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader); value = reader.readInt();
	}
	

}