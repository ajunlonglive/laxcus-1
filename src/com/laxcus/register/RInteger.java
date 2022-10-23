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
 * 整型参数
 *
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RInteger extends RParameter {

	/** 整型变量 **/
	private int value;

	/**
	 * 根据传入的整型值参数，生成它的副本
	 * @param that RInteger实例
	 */
	private RInteger(RInteger that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的整型参数
	 */
	public RInteger() {
		super(RParameterType.INTEGER);
	}

	/**
	 * 建立一个整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RInteger(String name, int value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RInteger(Naming name, int value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析整型数据
	 * @param reader 可类化读取器
	 */
	public RInteger(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置整形值
	 * @param i 整形值
	 */
	public void setValue(int i) {
		value = i;
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
	 * @see com.laxcus.distribute.parameter.RParameter#duplicate()
	 */
	@Override
	public RInteger duplicate() {
		return new RInteger(this);
	}

	/**
	 * 将整型值写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer); writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析整型值
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader); value = reader.readInt();
	}

}