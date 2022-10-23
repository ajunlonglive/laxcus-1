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
 * 短整型参数
 *
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RShort extends RParameter {

	/** 短整型变量 **/
	private short value;

	/**
	 * 根据传入的短整型值参数，生成它的副本
	 * @param that RShort实例
	 */
	private RShort(RShort that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的短整型参数
	 */
	public RShort() {
		super(RParameterType.SHORT);
	}

	/**
	 * 建立一个短整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RShort(String name, short value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 建立一个短整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RShort(Naming name, short value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public RShort(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置短整型参数
	 * @param i 短整型参数
	 */
	public void setValue(short i) {
		value = i;
	}

	/**
	 * 返回短整型参数
	 * @return 短整型参数
	 */
	public short getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.RParameter#duplicate()
	 */
	@Override
	public RShort duplicate() {
		return new RShort(this);
	}

	/**
	 * 将短整型写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer); writer.writeShort(value);
	}

	/**
	 * 从可类化读取器中解析数据
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader); value = reader.readShort();
	}

}