/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function;

/**
 * 函数异常。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/17/2012
 * @since laxcus 1.0
 */
public class FunctionException extends RuntimeException {

	private static final long serialVersionUID = -1093499100657057898L;

	/**
	 * 构造默认的函数异常
	 */
	public FunctionException() {
		super();
	}

	/**
	 * 构造函数异常，指定错误信息
	 * @param message 错误信息
	 */
	public FunctionException(String message) {
		super(message);
	}

	/**
	 * 构造函数异常，格式化错误信息
	 * @param format 格式化描述
	 * @param args 参数
	 */
	public FunctionException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造函数异常，包容另一个异常
	 * @param cause 异常
	 */
	public FunctionException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造函数异常，包括当前的错误信息和异常堆栈
	 * @param message 错误信息
	 * @param cause 异常
	 */
	public FunctionException(String message, Throwable cause) {
		super(message, cause);
	}

}