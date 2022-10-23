/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.naming;

/**
 * 阶段命名异常。
 * 
 * @author scott.liang
 * @version 1.0 02/23/2009
 * @since laxcus 1.0
 */
public class IllegalPhaseException extends IllegalArgumentException {

	private static final long serialVersionUID = -4196707308694470705L;

	/**
	 * 构造默认的阶段命名异常
	 */
	public IllegalPhaseException() {
		super();
	}

	/**
	 * 构造阶段命名异常，指定错误信息
	 * @param message 异常信息
	 */
	public IllegalPhaseException(String message) {
		super(message);
	}

	/**
	 * 构造阶段命名异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public IllegalPhaseException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造阶段命名异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public IllegalPhaseException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造阶段命名异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 异常实例
	 */
	public IllegalPhaseException(String message, Throwable cause) {
		super(message, cause);
	}

}