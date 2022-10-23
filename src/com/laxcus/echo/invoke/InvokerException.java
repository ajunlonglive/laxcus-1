/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * 调用器异常
 * 
 * @author scott.liang
 * @version 1.0 05/30/2009
 * @since laxcus 1.0
 */
public class InvokerException extends RuntimeException {

	private static final long serialVersionUID = 9019601038019808769L;

	/**
	 * 构造默认的调用器异常
	 */
	public InvokerException() {
		super();
	}

	/**
	 * 构造调用器异常，记录错误信息
	 * @param message
	 */
	public InvokerException(String message) {
		super(message);
	}

	/**
	 * 构造调用器异常，格式化错误信息
	 * @param format
	 * @param args
	 */
	public InvokerException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造调用器异常，叠加另一个异常
	 * @param cause
	 */
	public InvokerException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造调用器异常，叠加异常和错误消息
	 * @param message
	 * @param cause
	 */
	public InvokerException(String message, Throwable cause) {
		super(message, cause);
	}

}