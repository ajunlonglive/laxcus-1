/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 查找数据库配置命令。返回一个数据库配置实例资源。
 * 命令格式："find schema [schema name]"
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since 1.0
 */
public class FindSchema extends GuessSchema {

	private static final long serialVersionUID = -7099458328841844473L;

	/**
	 * 根据传入的查找数据库命令，生成它的数据副本
	 * @param that FindSchema实例
	 */
	private FindSchema(FindSchema that) {
		super(that);
	}

	/**
	 * 构造查找数据库命令，指定数据库名称
	 * @param fame 数据库名称
	 */
	public FindSchema(Fame fame) {
		super();
		setFame(fame);
	}

	/**
	 * 从可类化读取器中解析查找数据库命令参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FindSchema(ClassReader reader) {
		super();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindSchema duplicate() {
		return new FindSchema(this);
	}

}