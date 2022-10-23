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
 * 删除数据库命令。<br>
 * 命令格式：”drop schema [schema name]“
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class DropSchema extends GuessSchema {

	private static final long serialVersionUID = 6914508159706498857L;

	/**
	 * 构造默认和私有的删除数据库命令
	 */
	private DropSchema() {
		super();
	}

	/**
	 * 根据传入删除数据库命令的实例，生成它的数据副本
	 * @param that DropSchema实例
	 */
	private DropSchema(DropSchema that) {
		super(that);
	}

	/**
	 * 构造删除数据库命令，指定数据库名称
	 * @param fame Fame实例
	 */
	public DropSchema(Fame fame) {
		this();
		setFame(fame);
	}

	/**
	 * 从可类化读取器中解析删除数据库命令参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public DropSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据当前的删除数据库命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropSchema duplicate() {
		return new DropSchema(this);
	}
}