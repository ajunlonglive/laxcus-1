/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import java.util.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 字节数组参数
 * 
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RRaw extends RParameter {

	/** 字节数组参数 **/
	private byte[] value;

	/**
	 * 根据传入的字节数组参数，生成它的副本
	 * @param that RRaw实例
	 */
	private RRaw(RRaw that) {
		super(that);
		setValue(that.value);
	}

	/**
	 * 建立一个默认的字节数组参数
	 */
	public RRaw() {
		super(RParameterType.RAW);
	}

	/**
	 * 建立一个字节数组参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RRaw(String name, byte[] value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 建立一个字节数组参数，同时指定它的名称和数值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RRaw(Naming name, byte[] value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public RRaw(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置字节数组
	 * @param b 字节数组
	 */
	public void setValue(byte[] b) {
		if (b == null) {
			value = null;
		} else {
			value = Arrays.copyOfRange(b, 0, b.length);
		}
	}

	/**
	 * 返回字节数组
	 * @return 字节数组
	 */
	public byte[] getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.RParameter#duplicate()
	 */
	@Override
	public RRaw duplicate() {
		return new RRaw(this);
	}

	/**
	 * 将二进制数据写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeByteArray(value);
	}
	
	/**
	 * 从可类化读取器中解析二进制数据
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		value = reader.readByteArray();
	}
	

}