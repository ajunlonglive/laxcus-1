/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

/**
 * 不正确的实例异常。
 * 
 * @author scott.liang
 * @version 1.0 9/26/2021
 * @since laxcus 1.0
 */
public class IllegalInstanceException extends IllegalArgumentException {

	private static final long serialVersionUID = -1921729156500976364L;

	/**
	 * 构造默认的不正确的实例异常
	 */
	public IllegalInstanceException() {
		super();
	}

	/**
	 * 构造不正确的实例异常，指定错误信息
	 * @param message 异常信息
	 */
	public IllegalInstanceException(String message) {
		super(message);
	}

	/**
	 * 构造不正确的实例异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public IllegalInstanceException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造不正确的实例异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public IllegalInstanceException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造不正确的实例异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public IllegalInstanceException(String message, Throwable cause) {
		super(message, cause);
	}

}