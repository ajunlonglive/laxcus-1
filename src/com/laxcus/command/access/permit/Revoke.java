/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.util.classable.*;

/**
 * 回收用户权限命令。<br><br>
 * 
 * 命令格式：<br>
 * 1. 回收用户权限：REVOKE [options] FROM username1,username2,... <br>
 * 2. 回收用户的数据库权限：REVOKE [options] ON SCHEMA schema1,schema2,... FROM username1,username2,... <br>
 * 3. 回收用户的数据表权限：REVOKE [options] ON schema.table FROM username1, username2,... <br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class Revoke extends Certificate {

	private static final long serialVersionUID = 4829512647919012012L;

	/**
	 * 根据传入的回收用户权限命令，生成它的数据副本
	 * @param that Revoke实例
	 */
	private Revoke(Revoke that) {
		super(that);
	}

	/**
	 * 构造默认的回收用户权限命令
	 */
	public Revoke() {
		super();
	}

	/**
	 * 从可类化读取器中解析回收用户权限命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public Revoke(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据当前回收用户权限命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Revoke duplicate() {
		return new Revoke(this);
	}

}