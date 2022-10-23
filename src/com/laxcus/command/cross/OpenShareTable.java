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
 * 开放数据表共享资源<br>
 * 
 * 语法格式：OPEN SHARE TABLE [ALL | 表名1, 表名2, ...] ON [SELECT,INSERT,DELETE,UPDATE] TO [用户名 | SIGN 用户签名 , ...]
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public class OpenShareTable extends ShareTable {

	private static final long serialVersionUID = 5077972904914554227L;

	/**
	 * 构造默认的开放数据表共享资源
	 */
	public OpenShareTable() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析开放数据表共享资源命令
	 * @param reader 可类化数据读取器
	 */
	public OpenShareTable(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成开放数据表共享资源的数据副本
	 * 
	 * @param that OpenShareTable实例
	 */
	private OpenShareTable(OpenShareTable that) {
		super(that);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public OpenShareTable duplicate() {
		return new OpenShareTable(this);
	}

}