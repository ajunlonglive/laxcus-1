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
 * 日期参数
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class InputDate extends InputParameter {

	private static final long serialVersionUID = -4913896370189628166L;

	/** 日期参数 **/
	private int value;

	/**
	 * 根据传入的日期参数，生成它的副本
	 * @param that
	 */
	private InputDate(InputDate that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的日期参数
	 */
	public InputDate() {
		super(InputParameterType.DATE);
		value = 0;
	}

	/**
	 * 建立一个默认的日期参数，同时指定名称和参数
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputDate(String name, int value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个默认的日期参数，同时指定名称和参数
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputDate(String name, int value, String tooltip) {
		this(name, value);
		setTooltip(tooltip);
	}
	
	/**
	 * 从可类化读取器中解析日期参数
	 * @param reader 可类化读取器
	 */
	public InputDate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置参数值
	 * @param i 参数值
	 */
	public void setValue(int i) {
		value = i;
		// 参数置为有效
		setEnabled(true);
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
	 * @see com.laxcus.distribute.parameter.BootParameter#duplicate()
	 */
	@Override
	public InputDate duplicate() {
		return new InputDate(this);
	}

	/**
	 * 将日期参数写入可类化写入器
	 * @see com.laxcus.task.guide.parameter.InputParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析日期参数
	 * @see com.laxcus.task.guide.parameter.InputParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readInt();
	}

}