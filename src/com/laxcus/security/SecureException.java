/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */ 
package com.laxcus.security;

import java.io.*;

/**
 * 安全操作异常
 * 
 * @author scott.liang
 * @version 1.0 10/28/2009
 * @since laxcus 1.0
 */
public class SecureException extends IOException {

	private static final long serialVersionUID = 6378154590602117154L;

	/**
	 * 构造默认的安全操作异常
	 */
	public SecureException() {
		super();
	}

	/**
	 * 构造安全操作异常，指定错误信息
	 * @param message 异常信息
	 */
	public SecureException(String message) {
		super(message);
	}

	/**
	 * 构造安全操作异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public SecureException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造安全操作异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public SecureException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造安全操作异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public SecureException(String message, Throwable cause) {
		super(message, cause);
	}

}