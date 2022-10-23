/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 可类化对象实例参数。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RClassable extends RParameter {

	/** 可类化接口实现对象 **/
	private Classable value;

	/**
	 * 根据传入的对象，生成它的副本
	 * @param that RClassable实例
	 */
	private RClassable(RClassable that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的可类化对象实例
	 */
	public RClassable() {
		super(RParameterType.CLASSABLE);
	}

	/**
	 * 建立一个可类化对象实例，同时指定它的标题和参数
	 * @param name 参数名称
	 * @param value 可类化对象实例
	 */
	public RClassable(String name, Classable value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 建立一个可类化对象实例，同时指定它的标题和参数
	 * @param name 参数名称
	 * @param value 可类化对象实例
	 */
	public RClassable(Naming name, Classable value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 从可类化读取器中解析数据
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RClassable(ClassReader reader) {
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
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeDefault(value);
	}

	/**
	 * 从可类化读取器中解析一个可类化接口对象
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		value = reader.readDefault();
	}

	/**
	 * 生成当前可类化对象的实例副本
	 * @see com.laxcus.distribute.parameter.RParameter#duplicate()
	 */
	@Override
	public RClassable duplicate() {
		return new RClassable(this);
	}

}