/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

/**
 * 列属性解析异常
 * 
 * @author scott.liang
 * @version 1.0 6/1/2009
 * @since laxcus 1.0
 */
public class ColumnAttributeResolveException extends RuntimeException {

	private static final long serialVersionUID = -9035222311842400254L;

	/**
	 * 构造默认的列属性解析异常
	 */
	public ColumnAttributeResolveException() {
		super();
	}

	/**
	 * 构造列属性解析异常，指定错误信息
	 * @param message 异常信息
	 */
	public ColumnAttributeResolveException(String message) {
		super(message);
	}

	/**
	 * 构造列属性解析异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ColumnAttributeResolveException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造列属性解析异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public ColumnAttributeResolveException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造列属性解析异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public ColumnAttributeResolveException(String message, Throwable cause) {
		super(message, cause);
	}

}