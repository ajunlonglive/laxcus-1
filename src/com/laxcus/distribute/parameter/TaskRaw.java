/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.parameter;

import java.util.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 字节数组参数
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskRaw extends TaskParameter {

	private static final long serialVersionUID = 8764754851500867457L;

	/** 字节数组参数 **/
	private byte[] value;

	/**
	 * 根据传入的字节数组参数，生成它的副本
	 * @param that TaskRaw实例
	 */
	private TaskRaw(TaskRaw that) {
		super(that);
		setValue(that.value);
	}

	/**
	 * 建立一个默认的字节数组参数
	 */
	public TaskRaw() {
		super(TaskParameterType.RAW);
	}

	/**
	 * 建立一个字节数组参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskRaw(String title, byte[] value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 建立一个字节数组参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskRaw(Naming title, byte[] value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public TaskRaw(ClassReader reader) {
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
	 * @see com.laxcus.distribute.value.TaskParameter#duplicate()
	 */
	@Override
	public TaskRaw duplicate() {
		return new TaskRaw(this);
	}

	/**
	 * 将二进制数据写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeByteArray(value);
	}
	
	/**
	 * 从可类化读取器中解析二进制数据
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readByteArray();
	}
	

}