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
 * 双浮点参数
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class InputDouble extends InputParameter {

	private static final long serialVersionUID = -7940531649789023909L;

	/** 参数值 **/
	private double value;

	/**
	 * 根据传入的双浮点参数，生成它的副本
	 * @param that BootDouble实例
	 */
	private InputDouble(InputDouble that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的双浮点参数
	 */
	public InputDouble() {
		super(InputParameterType.DOUBLE);
		value = 0.0f;
	}

	/**
	 * 建立一个双浮点参数，并且指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputDouble(String name, double value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 建立一个双浮点参数，并且指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputDouble(String name, double value, String tooltip) {
		this(name, value);
		setTooltip(tooltip);
	}
	
	/**
	 * 从可类化读取器中解析双浮点数
	 * @param reader 可类化读取器
	 */
	public InputDouble(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置双浮点值
	 * @param i 双浮点值
	 */
	public void setValue(double i) {
		value = i;
		// 参数置为有效
		setEnabled(true);
	}

	/**
	 * 取双浮点值
	 * @return 双浮点值
	 */
	public double getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.BootParameter#duplicate()
	 */
	@Override
	public InputDouble duplicate() {
		return new InputDouble(this);
	}

	/**
	 * 将双浮点值写入可类化写入器
	 * @see com.laxcus.task.guide.parameter.InputParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeDouble(value);
	}

	/**
	 * 从可类化读取器中解析双浮点值
	 * @see com.laxcus.task.guide.parameter.InputParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readDouble();
	}

}