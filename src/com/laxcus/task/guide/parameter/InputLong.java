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
 * 长整型参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class InputLong extends InputParameter {

	private static final long serialVersionUID = 8171551796182279160L;

	/** 长整型变量 **/
	private long value;

	/**
	 * 根据传入的长整型值参数，生成它的副本
	 * @param that BootLong实例
	 */
	private InputLong(InputLong that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的长整型参数
	 */
	public InputLong() {
		super(InputParameterType.LONG);
		value = 0;
	}

	/**
	 * 建立一个长整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputLong(String name, long value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个长整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public InputLong(String name, long value, String tooltip) {
		this(name, value);
		setTooltip(tooltip);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public InputLong(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置长整型变量
	 * @param i 长整型变量
	 */
	public void setValue(long i) {
		value = i;
		// 参数置为有效
		setEnabled(true);
	}

	/**
	 * 返回长整型变量
	 * @return 长整型变量
	 */
	public long getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.BootParameter#duplicate()
	 */
	@Override
	public InputLong duplicate() {
		return new InputLong(this);
	}

	/**
	 * 将长整数写入可类化写入器
	 * @see com.laxcus.task.guide.parameter.InputParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeLong(value);
	}

	/**
	 * 从可类化读取器中解析长整数
	 * @see com.laxcus.task.guide.parameter.InputParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readLong();
	}

}