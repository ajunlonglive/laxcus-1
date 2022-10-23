/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

/**
 * 分布任务没有找到异常。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 5/30/2013
 * @since laxcus 1.0
 */
public class TaskNotFoundException extends TaskException {

	private static final long serialVersionUID = 7209301760345412038L;

	/**
	 * 构造默认的分布任务没有找到异常
	 */
	public TaskNotFoundException() {
		super();
	}

	/**
	 * 构造分布任务没有找到异常，指定错误信息
	 * @param message 异常信息
	 */
	public TaskNotFoundException(String message) {
		super(message);
	}

	/**
	 * 构造分布任务没有找到异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TaskNotFoundException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造分布任务没有找到异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TaskNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造分布任务没有找到异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TaskNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}