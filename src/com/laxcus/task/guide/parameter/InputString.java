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
 * 字符串变量
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class InputString extends InputParameter {

	private static final long serialVersionUID = -1128879279418878665L;

	/** 字符串变量 */
	private String value;

	/**
	 * 根据传入的字符串值参数，生成它的副本
	 * @param that BootString实例
	 */
	private InputString(InputString that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的字符串参数
	 */
	protected InputString() {
		super(InputParameterType.STRING);
		value = null;
	}

	/**
	 * 建立一个字符串参数，同时指定它的名称和数值
	 * @param name 参数名称
	 * @param value 字符串
	 */
	public InputString(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个字符串参数，指定它的名称、值、工具提示
	 * @param name 名称
	 * @param value
	 * @param tooltip
	 */
	public InputString(String name, String value, String tooltip) {
		this(name, value);
		this.setTooltip(tooltip);
	}
	
	/**
	 * 从可类化读取器中解析字符串
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public InputString(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置字符串值
	 * @param e 字符串值
	 */
	public void setValue(String e) {
		value = e;
		// 参数置为有效
		setEnabled(true);
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
	 * @see com.laxcus.distribute.value.BootParameter#duplicate()
	 */
	@Override
	public InputString duplicate() {
		return new InputString(this);
	}

	/*
	 * 将字符串写入可类化写入器
	 * @see com.laxcus.distribute.value.BootParameter#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(this.value);
	}

	/*
	 * 从可类化读取器中读取字节串
	 * @see com.laxcus.distribute.value.BootParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		this.value = reader.readString();
	}
	

}