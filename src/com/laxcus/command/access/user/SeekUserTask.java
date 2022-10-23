/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.classable.*;

/**
 * 检索用户的阶段命名
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class SeekUserTask extends SeekUserResource {

	private static final long serialVersionUID = 5046980564956984736L;

	/**
	 * 构造默认的检索用户的阶段命名
	 */
	public SeekUserTask() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析检索用户的阶段命名
	 * @param reader 可类化数据读取器
	 */
	public SeekUserTask(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成检索用户的阶段命名的数据副本
	 * @param that 检索用户的阶段命名
	 */
	private SeekUserTask(SeekUserTask that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SeekUserTask duplicate() {
		return new SeekUserTask(this);
	}

}