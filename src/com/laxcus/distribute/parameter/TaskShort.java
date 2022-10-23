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
 * 短整型参数
 *
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskShort extends TaskParameter {

	private static final long serialVersionUID = 8549455449147278658L;

	/** 短整型变量 **/
	private short value;

	/**
	 * 根据传入的短整型值参数，生成它的副本
	 * @param that TaskShort实例
	 */
	private TaskShort(TaskShort that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的短整型参数
	 */
	public TaskShort() {
		super(TaskParameterType.SHORT);
	}

	/**
	 * 建立一个短整型参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskShort(String title, short value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 建立一个短整型参数，同时指定它的名称和数值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskShort(Naming title, short value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public TaskShort(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置短整型参数
	 * @param i 短整型参数
	 */
	public void setValue(short i) {
		value = i;
	}

	/**
	 * 返回短整型参数
	 * @return 短整型参数
	 */
	public short getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.TaskParameter#duplicate()
	 */
	@Override
	public TaskShort duplicate() {
		return new TaskShort(this);
	}

	/**
	 * 将短整型写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeShort(value);
	}

	/**
	 * 从可类化读取器中解析数据
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readShort();
	}

}