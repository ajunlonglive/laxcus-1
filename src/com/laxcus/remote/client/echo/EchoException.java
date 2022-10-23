/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

/**
 * 异步处理异常
 * 
 * @author scott.liang
 * @version 1.0 12/9/2011
 * @since laxcus 1.0
 */
public class EchoException extends RuntimeException {

	private static final long serialVersionUID = 6938685063629949047L;

	/**
	 * 构造默认的异步处理异常
	 */
	public EchoException() {
		super();
	}

	/**
	 * 构造异步处理异常，指定错误信息
	 * @param message 异常信息
	 */
	public EchoException(String message) {
		super(message);
	}

	/**
	 * 构造异步处理异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public EchoException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造异步处理异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public EchoException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造异步处理异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public EchoException(String message, Throwable cause) {
		super(message, cause);
	}

}