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
 * 单浮点参数
 *
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RFloat extends RParameter {

	/** 单浮点值 **/
	private float value;

	/**
	 * 根据传入的单浮点参数，生成它的副本
	 * @param that RFloat实例
	 */
	private RFloat(RFloat that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的单浮点参数
	 */
	public RFloat() {
		super(RParameterType.FLOAT);
	}

	/**
	 * 建立一个单浮点参数，同时指定名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RFloat(String name, float value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 建立一个单浮点参数，同时指定名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RFloat(Naming name, float value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析浮点数
	 * @param reader 可类化读取器
	 */
	public RFloat(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置单浮点值
	 * @param i 单浮点值
	 */
	public void setValue(float i) {
		value = i;
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
	 * @see com.laxcus.distribute.parameter.RParameter#duplicate()
	 */
	@Override
	public RFloat duplicate() {
		return new RFloat(this);
	}

	/**
	 * 将浮点数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer); writer.writeFloat(value);
	}

	/**
	 * 从可类化读取器中解析浮点值
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader); value = reader.readFloat();
	}

}