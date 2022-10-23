/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.parameter;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 单浮点参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskFloat extends TaskParameter {

	private static final long serialVersionUID = 2313019343245406880L;

	/** 单浮点值 **/
	private float value;

	/**
	 * 根据传入的单浮点参数，生成它的副本
	 * @param that TaskFloat实例
	 */
	private TaskFloat(TaskFloat that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的单浮点参数
	 */
	public TaskFloat() {
		super(TaskParameterType.FLOAT);
	}

	/**
	 * 建立一个单浮点参数，同时指定名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskFloat(String title, float value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 建立一个单浮点参数，同时指定名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskFloat(Naming title, float value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析浮点数
	 * @param reader 可类化读取器
	 */
	public TaskFloat(ClassReader reader) {
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
	 * @see com.laxcus.distribute.parameter.TaskParameter#duplicate()
	 */
	@Override
	public TaskFloat duplicate() {
		return new TaskFloat(this);
	}

	/**
	 * 将浮点数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeFloat(value);
	}

	/**
	 * 从可类化读取器中解析浮点值
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readFloat();
	}

}