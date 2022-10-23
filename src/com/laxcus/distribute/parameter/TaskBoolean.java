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
 * 布尔值参数
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskBoolean extends TaskParameter {

	private static final long serialVersionUID = -7048138881560320362L;

	/** 逻辑参数 **/
	private boolean value;

	/**
	 * 根据传入的布尔值参数，生成它的副本
	 * @param that
	 */
	private TaskBoolean(TaskBoolean that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的布尔值参数
	 */
	public TaskBoolean() {
		super(TaskParameterType.BOOLEAN);
	}

	/**
	 * 建立一个布尔值参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskBoolean(String title, boolean value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 建立一个布尔值参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskBoolean(Naming title, boolean value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 从可类化读取器中解析布尔值
	 * @param reader 可类化读取器
	 */
	public TaskBoolean(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置布尔参数
	 * @param b 布尔参数
	 */
	public void setValue(boolean b) {
		value = b;
	}

	/**
	 * 返回布尔参数
	 * @return 布尔参数
	 */
	public boolean getValue() {
		return value;
	}

	/**
	 * 判断是真
	 * @return 是真
	 */
	public boolean isTrue() {
		return value;
	}

	/**
	 * 判断是假
	 * @return 是假
	 */
	public boolean isFalse() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.TaskParameter#duplicate()
	 */
	@Override
	public TaskBoolean duplicate() {
		return new TaskBoolean(this);
	}

	/**
	 * 将布尔参数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(value);
	}

	/**
	 * 从可类化读取器中解析布尔参数
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readBoolean();
	}
	
}