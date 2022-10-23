/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

/**
 * 没找到列成员
 * 
 * @author scott.liang
 * @version 1.0 5/1/2012
 * @since laxcus 1.0
 */
public class ColumnNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 8900822969232687515L;

	/**
	 * 构造默认的没有找到列异常
	 */
	public ColumnNotFoundException() {
		super();
	}

	/**
	 * 构造没有找到列异常，指定错误信息
	 * @param message 异常信息
	 */
	public ColumnNotFoundException(String message) {
		super(message);
	}

	/**
	 * 构造没有找到列异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ColumnNotFoundException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造没有找到列异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public ColumnNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造没有找到列异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public ColumnNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}