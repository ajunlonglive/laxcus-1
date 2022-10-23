/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.util.classable.*;

/**
 * 输出某个账号下的全部数据库命名。<br>
 * 命令格式："fill all schema"
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class FillSchema extends Command {

	private static final long serialVersionUID = 5067260828445174602L;

	/**
	 * 根据传入的命令，生成它的数据副本。
	 * @param that FillSchema实例
	 */
	private FillSchema(FillSchema that) {
		super(that);
	}

	/**
	 * 构造默认的"fill schema"命令
	 */
	public FillSchema() {
		super();
	}

	/**
	 * 构造"fill schema"命令，指定它的回显地址
	 * @param cabin 回显地址
	 */
	public FillSchema(Cabin cabin) {
		this();
		setSource(cabin);
	}

	/**
	 * 从可类化数据读取器中解析"fill schema"命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FillSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FillSchema duplicate() {
		return new FillSchema(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

	}

}