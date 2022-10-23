/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

/**
 * 不正确的参数异常。
 * 
 * @author scott.liang
 * @version 1.0 03/23/2013
 * @since laxcus 1.0
 */
public class IllegalValueException extends IllegalArgumentException {

	private static final long serialVersionUID = 2175566150609293068L;

	/**
	 * 构造默认的不正确的参数异常
	 */
	public IllegalValueException() {
		super();
	}

	/**
	 * 构造不正确的参数异常，指定错误信息
	 * @param message 异常信息
	 */
	public IllegalValueException(String message) {
		super(message);
	}

	/**
	 * 构造不正确的参数异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public IllegalValueException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造不正确的参数异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public IllegalValueException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造不正确的参数异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public IllegalValueException(String message, Throwable cause) {
		super(message, cause);
	}

}