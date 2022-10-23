/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.parameter;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 字符串变量
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskString extends TaskParameter {

	private static final long serialVersionUID = -1128879279418878665L;

	/** 字符串变量 */
	private String value;

	/**
	 * 根据传入的字符串值参数，生成它的副本
	 * @param that TaskString实例
	 */
	private TaskString(TaskString that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的字符串参数
	 */
	protected TaskString() {
		super(TaskParameterType.STRING);
	}

	/**
	 * 建立一个字符串参数，同时指定它的名称和数值
	 * @param title 参数名称
	 * @param value 字符串
	 */
	public TaskString(String title, String value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 建立一个字符串实例，同时指定它的标题和参数
	 * @param title 参数名称
	 * @param value 字符串
	 */
	public TaskString(Naming title, String value) {
		this();
		setName(title);
		setValue(value);
	}
	
	/**
	 * 从可类化读取器中解析字符串
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TaskString(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置字符串值
	 * @param e 字符串值
	 */
	public void setValue(String e) {
		Laxkit.nullabled(e);

		value = e;
	}

	/**
	 * 返回字符串值
	 * @return 字符串值
	 */
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.TaskParameter#duplicate()
	 */
	@Override
	public TaskString duplicate() {
		return new TaskString(this);
	}

	/*
	 * 将字符串写入可类化写入器
	 * @see com.laxcus.distribute.value.TaskParameter#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeString(this.value);
	}

	/*
	 * 从可类化读取器中读取字节串
	 * @see com.laxcus.distribute.value.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		this.value = reader.readString();
	}
	

}