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
 * 时间戳参数
 *
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RTimestamp extends RParameter {

	/** 时间戳值 **/
	private long value;

	/**
	 * 根据传入的分布时间戳参数，生成一个它的副本
	 * @param that RTimestamp实例
	 */
	private RTimestamp(RTimestamp that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个分布时间戳参数
	 */
	public RTimestamp() {
		super(RParameterType.TIMESTAMP);
	}

	/**
	 * 建立一个分布时间戳参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RTimestamp(String name, long value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个分布时间戳参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RTimestamp(Naming name, long value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析时间戳参数
	 * @param reader 可类化读取器
	 */
	public RTimestamp(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置时间戳参数
	 * @param i 时间戳
	 */
	public void setValue(long i) {
		value = i;
	}

	/**
	 * 返回时间戳参数
	 * @return 时间戳
	 */
	public long getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.register.RToken#duplicate()
	 */
	@Override
	public RTimestamp duplicate() {
		return new RTimestamp(this);
	}


	/*
	 * 将时间戮参数写入可类化写入器
	 * @see com.laxcus.register.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeLong(value);
	}

	/*
	 * 从可类化读取器中解析时间戳参数
	 * @see com.laxcus.register.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		value = reader.readLong();
	}
	
}