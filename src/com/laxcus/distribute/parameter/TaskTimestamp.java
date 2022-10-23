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
 * 时间戳参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskTimestamp extends TaskParameter {

	private static final long serialVersionUID = 2388042755216466541L;

	/** 时间戳值 **/
	private long value;

	/**
	 * 根据传入的分布时间戳参数，生成一个它的副本
	 * @param that TaskTimestamp实例
	 */
	private TaskTimestamp(TaskTimestamp that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个分布时间戳参数
	 */
	public TaskTimestamp() {
		super(TaskParameterType.TIMESTAMP);
	}

	/**
	 * 建立一个分布时间戳参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskTimestamp(String title, long value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 建立一个分布时间戳参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskTimestamp(Naming title, long value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析时间戳参数
	 * @param reader 可类化读取器
	 */
	public TaskTimestamp(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置时间戳参数
	 * @param i 时间戳
	 */
	public void setValue(long i) {
		value = i;
	}

	/**
	 * 返回时间戳参数
	 * @return 时间戳
	 */
	public long getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.TaskParameter#duplicate()
	 */
	@Override
	public TaskTimestamp duplicate() {
		return new TaskTimestamp(this);
	}

	/**
	 * 将时间戮参数写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeLong(value);
	}

	/**
	 * 从可类化读取器中解析时间戳参数
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readLong();
	}
	
}