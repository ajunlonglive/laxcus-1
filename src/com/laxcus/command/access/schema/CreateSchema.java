/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.access.schema.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建立数据库命令。
 * 
 * @author scott.liang
 * @version 1.2 8/23/2012
 * @since laxcus 1.0
 */
public class CreateSchema extends Command {

	private static final long serialVersionUID = -1134240617437813604L;

	/** 数据库配置 **/
	private Schema schema;

	/**
	 * 构造默认和私有的建立数据库命令
	 */
	private CreateSchema() {
		super();
	}

	/**
	 * 根据传入的命令，生成它的数据副本
	 * @param that CreateSchema实例
	 */
	private CreateSchema(CreateSchema that) {
		super(that);
		setSchema(that.schema);
	}

	/**
	 * 构造建立数据库命令，指定一个数据库配置
	 * @param e Schema实例
	 */
	public CreateSchema(Schema e) {
		this();
		setSchema(e);
	}

	/**
	 * 根据传入的可类化读取器，解析建库参数。
	 * @param reader 可类化读取器
	 */
	public CreateSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据库配置
	 * @param e Schema实例
	 */
	public void setSchema(Schema e) {
		Laxkit.nullabled(e);

		schema = e;
	}

	/**
	 * 返回数据库配置
	 * @return Schema实例
	 */
	public Schema getSchema() {
		return schema;
	}

	/**
	 * 根据当前建立数据库命令实例，生成它的数据副本。
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateSchema duplicate() {
		return new CreateSchema(this);
	}

	/**
	 * 将数据库配置写入可类化存储器
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(schema);
	}

	/**
	 * 从可类化读取器中解析数据库配置
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		schema = new Schema(reader);
	}

}