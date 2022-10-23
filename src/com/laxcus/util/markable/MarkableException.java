/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

/**
 * 标记化异常
 * 
 * @author scott.liang
 * @version 1.0 8/22/2017
 * @since laxcus 1.0
 */
public class MarkableException extends RuntimeException {

	private static final long serialVersionUID = -4378839008494144424L;

	/**
	 * 构造默认的标记化异常
	 */
	public MarkableException() {
		super();
	}

	/**
	 * 构造可类化异常，指定错误信息
	 * @param message 异常信息
	 */
	public MarkableException(String message) {
		super(message);
	}

	/**
	 * 构造可类化异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public MarkableException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造可类化异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public MarkableException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造可类化异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public MarkableException(String message, Throwable cause) {
		super(message, cause);
	}

}