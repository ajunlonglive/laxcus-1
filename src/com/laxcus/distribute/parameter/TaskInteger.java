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
 * 整型参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskInteger extends TaskParameter {

	private static final long serialVersionUID = 2739093912202670474L;

	/** 整型变量 **/
	private int value;

	/**
	 * 根据传入的整型值参数，生成它的副本
	 * @param that TaskInteger实例
	 */
	private TaskInteger(TaskInteger that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的整型参数
	 */
	public TaskInteger() {
		super(TaskParameterType.INTEGER);
	}

	/**
	 * 建立一个整型参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskInteger(String title, int value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 建立一个整型参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskInteger(Naming title, int value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析整型数据
	 * @param reader 可类化读取器
	 */
	public TaskInteger(ClassReader reader) {
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
	 * @see com.laxcus.distribute.parameter.TaskParameter#duplicate()
	 */
	@Override
	public TaskInteger duplicate() {
		return new TaskInteger(this);
	}

	/**
	 * 将整型值写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析整型值
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readInt();
	}

}