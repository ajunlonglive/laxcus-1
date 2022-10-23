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
 * 处理数据库命令 <br>
 * 
 * 基于单个数据库处理。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public abstract class GuessSchema extends Command {

	private static final long serialVersionUID = -3608710604872657143L;

	/** 数据库名称 **/
	private Fame fame;

	/**
	 * 构造处理数据库命令
	 */
	protected GuessSchema() {
		super();
	}

	/**
	 * 根据传入的处理数据库命令实例，生成它的数据副本
	 * @param that GuessSchema实例
	 */
	protected GuessSchema(GuessSchema that) {
		super(that);
		fame = that.fame.duplicate();
	}

	/**
	 * 构造处理数据库命令，指定数据库名称
	 * @param fame 数据库名称
	 */
	protected GuessSchema(Fame fame) {
		this();
		setFame(fame);
	}
	
	/**
	 * 设置数据库名称，不允许空值
	 * @param e Fame实例
	 */
	public void setFame(Fame e) {
		Laxkit.nullabled(e);

		fame = e;
	}

	/**
	 * 返回数据库名称
	 * @return Fame实例
	 */
	public Fame getFame() {
		return fame;
	}

	/**
	 * 将被处理的数据库名称写入可类化存储器
	 * @since 1.1
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(fame);
	}

	/**
	 * 从可类化读取器中解析被处理的数据库名称
	 * @since 1.1
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		fame = new Fame(reader);
	}

}