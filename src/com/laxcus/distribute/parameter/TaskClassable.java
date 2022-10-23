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
 * 可类化对象实例参数。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskClassable extends TaskParameter {

	private static final long serialVersionUID = -7049640023232563510L;

	/** 可类化接口实现对象 **/
	private Classable value;

	/**
	 * 根据传入的对象，生成它的副本
	 * @param that TaskClassable实例
	 */
	private TaskClassable(TaskClassable that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的可类化对象实例
	 */
	public TaskClassable() {
		super(TaskParameterType.CLASSABLE);
	}

	/**
	 * 建立一个可类化对象实例，同时指定它的标题和参数
	 * @param title 参数名称
	 * @param value 可类化对象实例
	 */
	public TaskClassable(String title, Classable value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 建立一个可类化对象实例，同时指定它的标题和参数
	 * @param title 参数名称
	 * @param value 可类化对象实例
	 */
	public TaskClassable(Naming title, Classable value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 从可类化读取器中解析数据
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TaskClassable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置可类化实例对象
	 * @param e Classable实例
	 */
	public void setValue(Classable e) {
		Laxkit.nullabled(e);

		value = e;
	}

	/**
	 * 返回可类化实例对象
	 * @return Classable实例
	 */
	public Classable getValue() {
		return value;
	}

	/**
	 * 将可类化接口对象写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		writer.writeDefault(value);
	}

	/**
	 * 从可类化读取器中解析一个可类化接口对象
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		value = reader.readDefault();
	}

	/**
	 * 生成当前可类化对象的实例副本
	 * @see com.laxcus.distribute.parameter.TaskParameter#duplicate()
	 */
	@Override
	public TaskClassable duplicate() {
		return new TaskClassable(this);
	}

}