/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

/**
 * 语法解析异常。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/23/2009
 * @since laxcus 1.0
 */
public class SyntaxException extends RuntimeException {

	private static final long serialVersionUID = 6938685063629949047L;

	/**
	 * 构造默认的语法解析异常
	 */
	public SyntaxException() {
		super();
	}

	/**
	 * 构造语法解析异常，指定错误信息
	 * @param message 异常信息
	 */
	public SyntaxException(String message) {
		super(message);
	}

	/**
	 * 构造语法解析异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public SyntaxException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造语法解析异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public SyntaxException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造语法解析异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public SyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

}