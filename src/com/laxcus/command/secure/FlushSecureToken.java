/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import com.laxcus.util.classable.*;

/**
 * 输出密钥令牌数据到磁盘保存。<br>
 * 
 * 这个命令只能由WATCH站点发起，分到不同的站点去执行。
 * 
 * @author scott.liang
 * @version 1.0 2/12/2021
 * @since laxcus 1.0
 */
public class FlushSecureToken extends ExecuteSecureToken {

	private static final long serialVersionUID = 5316801771373796204L;

	/**
	 * 构造默认的输出密钥令牌命令
	 */
	public FlushSecureToken() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析输出密钥令牌命令
	 * @param reader 可类化数据读取器
	 */
	public FlushSecureToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成输出密钥令牌命令的数据副本
	 * @param that FlushSecureToken实例
	 */
	private FlushSecureToken(FlushSecureToken that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FlushSecureToken duplicate() {
		return new FlushSecureToken(this);
	}

}