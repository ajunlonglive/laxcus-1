/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

/**
 * 分布组件访问底层库异常。<br><br>
 * 
 * 发生在调用动态链接库前，检查发生SQL参数异常，或者其他参数异常。
 * 
 * @author scott.liang
 * @version 1.0 12/12/2020
 * @since laxcus 1.0
 */
public class TaskAccessException extends TaskException {

	private static final long serialVersionUID = 2246405829686182489L;

	/**
	 * 构造默认的分布组件访问底层库异常
	 */
	public TaskAccessException() {
		super();
	}

	/**
	 * 构造分布组件访问底层库异常，指定错误信息
	 * @param message 异常信息
	 */
	public TaskAccessException(String message) {
		super(message);
	}

	/**
	 * 构造分布组件访问底层库异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TaskAccessException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造分布组件访问底层库异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TaskAccessException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造分布组件访问底层库异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TaskAccessException(String message, Throwable cause) {
		super(message, cause);
	}

}