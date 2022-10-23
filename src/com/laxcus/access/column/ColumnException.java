/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

/**
 * 列参数异常
 * 
 * @author scott.liang
 * @version 1.0 04/08/2009
 * @since laxcus 1.0
 */
public class ColumnException extends RuntimeException {

	private static final long serialVersionUID = 7077720550533141354L;

	/**
	 * 构造默认的列参数异常
	 */
	public ColumnException() {
		super();
	}

	/**
	 * 构造列参数异常，指定错误信息
	 * @param message 异常信息
	 */
	public ColumnException(String message) {
		super(message);
	}

	/**
	 * 构造列参数异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ColumnException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造列参数异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public ColumnException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造列参数异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public ColumnException(String message, Throwable cause) {
		super(message, cause);
	}

}