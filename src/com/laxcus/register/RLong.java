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
 * 长整型参数
 *
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RLong extends RParameter {

	/** 长整型变量 **/
	private long value;

	/**
	 * 根据传入的长整型值参数，生成它的副本
	 * @param that RLong实例
	 */
	private RLong(RLong that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的长整型参数
	 */
	public RLong() {
		super(RParameterType.LONG);
	}

	/**
	 * 建立一个长整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RLong(String name, long value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个长整型参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RLong(Naming name, long value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public RLong(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置长整型变量
	 * @param i 长整型变量
	 */
	public void setValue(long i) {
		value = i;
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
	 * @see com.laxcus.distribute.parameter.RParameter#duplicate()
	 */
	@Override
	public RLong duplicate() {
		return new RLong(this);
	}

	/**
	 * 将长整数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer); writer.writeLong(value);
	}

	/**
	 * 从可类化读取器中解析长整数
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader); value = reader.readLong();
	}

}