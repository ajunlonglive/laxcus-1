/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

/**
 * 正定义操作异常。
 * 
 * @author scott.liang
 * @version 1.0 10/30/2017
 * @since laxcus 1.0
 */
public class CustomException extends RuntimeException {

	private static final long serialVersionUID = -2668768351266243590L;

	/**
	 * 构造默认的正定义操作异常
	 */
	public CustomException() {
		super();
	}

	/**
	 * 构造正定义操作异常，指定错误信息
	 * @param message 异常信息
	 */
	public CustomException(String message) {
		super(message);
	}

	/**
	 * 构造正定义操作异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public CustomException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造正定义操作异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public CustomException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造正定义操作异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public CustomException(String message, Throwable cause) {
		super(message, cause);
	}

}