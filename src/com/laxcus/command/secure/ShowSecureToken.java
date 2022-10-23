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
 * 显示密钥令牌
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class ShowSecureToken extends ExecuteSecureToken {

	private static final long serialVersionUID = -11421515659370435L;

	/**
	 * 构造默认的显示密钥令牌命令
	 */
	public ShowSecureToken() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示密钥令牌命令
	 * @param reader 可类化数据读取器
	 */
	public ShowSecureToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成显示密钥令牌命令的数据副本
	 * @param that ShowSecureToken实例
	 */
	private ShowSecureToken(ShowSecureToken that) {
		super(that);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowSecureToken duplicate() {
		return new ShowSecureToken(this);
	}

}