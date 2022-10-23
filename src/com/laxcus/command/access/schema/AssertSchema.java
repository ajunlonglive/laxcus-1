/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.schema;

import com.laxcus.access.schema.*;
import com.laxcus.echo.*;
import com.laxcus.util.classable.*;

/**
 * 判断数据库存在命令。
 * 命令格式："assert schema 数据库名"
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public class AssertSchema extends GuessSchema {
	
	private static final long serialVersionUID = 6780746132322495024L;

	/**
	 * 根据传入的判断数据存在命令实例，生成它的数据副本
	 * @param that 命令实例
	 */
	private AssertSchema(AssertSchema that) {
		super(that);
	}

	/**
	 * 构造判断数据库命令，指定数据库名称
	 * @param fame 数据库名称
	 */
	public AssertSchema(Fame fame) {
		super();
		setFame(fame);
	}

	/**
	 * 构造判断数据库命令，指定数据库名称和回显地址
	 * @param fame 数据库名称
	 * @param cabin 回显地址
	 */
	public AssertSchema(Fame fame, Cabin cabin) {
		this(fame);
		setSource(cabin);
	}

	/**
	 * 从可类化读取器中解析判断数据库存在参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public AssertSchema(ClassReader reader) {
		super();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AssertSchema duplicate() {
		return new AssertSchema(this);
	}
}