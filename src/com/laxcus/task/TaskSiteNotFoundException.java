/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

/**
 * 没有找到有效的任务站点。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2013
 * @since laxcus 1.0
 */
public class TaskSiteNotFoundException extends TaskNotFoundException {

	private static final long serialVersionUID = 1230586299757113763L;

	/**
	 * 构造默认的没有找到任务站点异常
	 */
	public TaskSiteNotFoundException() {
		super();
	}

	/**
	 * 构造没有找到任务站点异常，说明异常信息
	 * @param message 异常信息
	 */
	public TaskSiteNotFoundException(String message) {
		super(message);
	}

	/**
	 * 构造没有找到任务站点异常，格式化异常信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TaskSiteNotFoundException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 在一个异常的基础上构造没有找到任务站点异常
	 * @param cause 另一个异常实例
	 */
	public TaskSiteNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * 在一个异常的基础上构造没有找到任务站点异常，指定提示信息和异常
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TaskSiteNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}