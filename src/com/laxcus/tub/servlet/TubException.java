/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

/**
 * 边缘处理异常。<br><br>
 * 
 * 边缘处理异常在边缘处理过程中产生。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/19/2019
 * @since laxcus 1.0
 */
public class TubException extends Exception {

	private static final long serialVersionUID = 2666860520511115812L;

	/**
	 * 构造默认的边缘处理异常
	 */
	public TubException() {
		super();
	}

	/**
	 * 构造边缘处理异常，指定错误信息
	 * @param message 异常信息
	 */
	public TubException(String message) {
		super(message);
	}

	/**
	 * 构造边缘处理异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TubException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造边缘处理异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TubException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造边缘处理异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TubException(String message, Throwable cause) {
		super(message, cause);
	}

}