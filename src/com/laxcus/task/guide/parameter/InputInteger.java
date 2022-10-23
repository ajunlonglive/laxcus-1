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
 * 整型参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class InputInteger extends InputParameter {

	private static final long serialVersionUID = 2739093912202670474L;

	/** 整型变量 **/
	private int value;

	/**
	 * 根据传入的整型值参数，生成它的副本
	 * @param that BootInteger实例
	 */
	private InputInteger(InputInteger that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的整型参数
	 */
	public InputInteger() {
		super(InputParameterType.INTEGER);
		value = 0;
	}

	/**
	 * 建立一个整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputInteger(String name, int value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 建立一个整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 * @param tooltip 工具提示
	 */
	public InputInteger(String name, int value, String tooltip) {
		this(name, value);
		setTooltip(tooltip);
	}

	/**
	 * 建立一个整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 * @param tooltip 工具提示
	 * @param boolean 选择
	 */
	public InputInteger(String name, int value, String tooltip, boolean select) {
		this(name, value, tooltip);
		setSelect(select);
	}
	
	/**
	 * 从可类化读取器中解析整型数据
	 * @param reader 可类化读取器
	 */
	public InputInteger(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置整形值
	 * @param i 整形值
	 */
	public void setValue(int i) {
		value = i;
		// 参数置为有效
		setEnabled(true);
	}

	/**
	 * 返回整形值
	 * @return 整形值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.BootParameter#duplicate()
	 */
	@Override
	public InputInteger duplicate() {
		return new InputInteger(this);
	}

	/**
	 * 将整型值写入可类化写入器
	 * @see com.laxcus.task.guide.parameter.InputParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析整型值
	 * @see com.laxcus.task.guide.parameter.InputParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readInt();
	}

}