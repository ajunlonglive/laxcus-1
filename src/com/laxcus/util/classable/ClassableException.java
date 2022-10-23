/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.classable;

/**
 * 可类化异常
 * 
 * @author scott.liang
 * @version 1.0 3/29/2015
 * @since laxcus 1.0
 */
public class ClassableException extends RuntimeException {

	private static final long serialVersionUID = -4378839008494144424L;

	/**
	 * 构造默认的可类化异常
	 */
	public ClassableException() {
		super();
	}

	/**
	 * 构造可类化异常，指定错误信息
	 * @param message 异常信息
	 */
	public ClassableException(String message) {
		super(message);
	}

	/**
	 * 构造可类化异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ClassableException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造可类化异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public ClassableException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造可类化异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public ClassableException(String message, Throwable cause) {
		super(message, cause);
	}

}