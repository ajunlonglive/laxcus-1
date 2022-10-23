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
 * 日期参数
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskDate extends TaskParameter {

	private static final long serialVersionUID = -4913896370189628166L;

	/** 日期参数 **/
	private int value;

	/**
	 * 根据传入的日期参数，生成它的副本
	 * @param that
	 */
	private TaskDate(TaskDate that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的日期参数
	 */
	public TaskDate() {
		super(TaskParameterType.DATE);
	}

	/**
	 * 建立一个默认的日期参数，同时指定名称和参数
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskDate(String title, int value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 建立一个默认的日期参数，同时指定名称和参数
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskDate(Naming title, int value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析日期参数
	 * @param reader 可类化读取器
	 */
	public TaskDate(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置参数值
	 * @param i 参数值
	 */
	public void setValue(int i) {
		value = i;
	}

	/**
	 * 返回参数值
	 * @return 参数值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.parameter.TaskParameter#duplicate()
	 */
	@Override
	public TaskDate duplicate() {
		return new TaskDate(this);
	}

	/**
	 * 将日期参数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析日期参数
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readInt();
	}

}