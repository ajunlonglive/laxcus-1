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
 * 单浮点参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class InputFloat extends InputParameter {

	private static final long serialVersionUID = 2313019343245406880L;

	/** 单浮点值 **/
	private float value;

	/**
	 * 根据传入的单浮点参数，生成它的副本
	 * @param that BootFloat实例
	 */
	private InputFloat(InputFloat that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的单浮点参数
	 */
	public InputFloat() {
		super(InputParameterType.FLOAT);
		value = 0.0f;
	}

	/**
	 * 建立一个单浮点参数，同时指定名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputFloat(String name, float value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个单浮点参数，同时指定名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputFloat(String name, float value, String tooltip) {
		this(name, value);
		setTooltip(tooltip);
	}
	
	/**
	 * 从可类化读取器中解析浮点数
	 * @param reader 可类化读取器
	 */
	public InputFloat(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置单浮点值
	 * @param i 单浮点值
	 */
	public void setValue(float i) {
		value = i;
		// 参数置为有效
		setEnabled(true);
	}

	/**
	 * 返回单浮点值
	 * @return 单浮点值
	 */
	public float getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.BootParameter#duplicate()
	 */
	@Override
	public InputFloat duplicate() {
		return new InputFloat(this);
	}

	/**
	 * 将浮点数写入可类化写入器
	 * @see com.laxcus.task.guide.parameter.InputParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeFloat(value);
	}

	/**
	 * 从可类化读取器中解析浮点值
	 * @see com.laxcus.task.guide.parameter.InputParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readFloat();
	}

}