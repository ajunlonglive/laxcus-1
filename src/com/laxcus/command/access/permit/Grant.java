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
 * 用户授权命令。<br><br>
 * 
 * 命令格式：<br>
 * 1. 授权用户：GRANT [options] TO username <br>
 * 2. 授权数据库：GRANT [operator] ON SCHEMA schemaname TO username <br>
 * 3. 授权数据表：GRANT [operator] ON schema.table TO username <br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class Grant extends Certificate {

	private static final long serialVersionUID = 4980302273497758482L;

	/**
	 * 根据传入的用户授权命令，生成它的数据副本
	 * @param that Grant实例
	 */
	private Grant(Grant that) {
		super(that);
	}
	
	/**
	 * 构造默认的用户授权命令
	 */
	public Grant() {
		super();
	}

	/**
	 * 从可类化读取器中解析用户授权命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public Grant(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 根据当前用户授权命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Grant duplicate() {
		return new Grant(this);
	}

}
