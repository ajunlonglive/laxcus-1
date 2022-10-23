/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.parameter;

import com.laxcus.util.classable.*;

/**
 * 时间戳参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class InputTimestamp extends InputParameter {

	private static final long serialVersionUID = 2388042755216466541L;

	/** 时间戳值 **/
	private long value;

	/**
	 * 根据传入的分布时间戳参数，生成一个它的副本
	 * @param that BootTimestamp实例
	 */
	private InputTimestamp(InputTimestamp that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个分布时间戳参数
	 */
	public InputTimestamp() {
		super(InputParameterType.TIMESTAMP);
		value = 0;
	}

	/**
	 * 建立一个分布时间戳参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputTimestamp(String name, long value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个分布时间戳参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputTimestamp(String name, long value, String tooltipe) {
		this(name, value);
		setTooltip(tooltipe);
	}
	
	/**
	 * 从可类化读取器中解析时间戳参数
	 * @param reader 可类化读取器
	 */
	public InputTimestamp(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置时间戳参数
	 * @param i 时间戳
	 */
	public void setValue(long i) {
		value = i;
		// 参数置为有效
		setEnabled(true);
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
	 * @see com.laxcus.distribute.value.BootParameter#duplicate()
	 */
	@Override
	public InputTimestamp duplicate() {
		return new InputTimestamp(this);
	}

	/**
	 * 将时间戮参数写入可类化写入器
	 * @see com.laxcus.task.guide.parameter.InputParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeLong(value);
	}

	/**
	 * 从可类化读取器中解析时间戳参数
	 * @see com.laxcus.task.guide.parameter.InputParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readLong();
	}
	
}