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
 * 关闭数据库共享资源<br>
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public class CloseShareSchema extends ShareSchema {

	private static final long serialVersionUID = -174298457947786099L;

	/**
	 * 构造默认的关闭数据库共享资源
	 */
	public CloseShareSchema() {
		super();
	}
	
	/**
	 * 从可类化数据读取器中解析关闭数据库共享资源命令
	 * @param reader 可类化数据读取器
	 */
	public CloseShareSchema(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成关闭数据库共享资源的数据副本
	 * 
	 * @param that CloseShareSchema实例
	 */
	private CloseShareSchema(CloseShareSchema that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CloseShareSchema duplicate() {
		return new CloseShareSchema(this);
	}

}