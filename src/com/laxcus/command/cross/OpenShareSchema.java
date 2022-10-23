/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.util.classable.*;

/**
 * 开放数据库共享资源<br>
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public class OpenShareSchema extends ShareSchema {

	private static final long serialVersionUID = 7403308790205829775L;

	/**
	 * 构造默认的开放数据库共享资源
	 */
	public OpenShareSchema() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析开放数据库共享资源命令
	 * @param reader 可类化数据读取器
	 */
	public OpenShareSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成开放数据库共享资源的数据副本
	 * 
	 * @param that 开放数据库共享资源实例
	 */
	private OpenShareSchema(OpenShareSchema that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public OpenShareSchema duplicate() {
		return new OpenShareSchema(this);
	}

}