/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

/**
 * 前端任务不支持异常。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/23/2019
 * @since laxcus 1.0
 */
public class MissionUnsupportedException extends MissionException {

	private static final long serialVersionUID = 1569242383634685239L;

	/**
	 * 构造默认的前端任务不支持异常
	 */
	public MissionUnsupportedException() {
		super();
	}

	/**
	 * 构造前端任务不支持异常，指定错误信息
	 * @param message 异常信息
	 */
	public MissionUnsupportedException(String message) {
		super(message);
	}

	/**
	 * 构造前端任务不支持异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public MissionUnsupportedException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造前端任务不支持异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public MissionUnsupportedException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造前端任务不支持异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public MissionUnsupportedException(String message, Throwable cause) {
		super(message, cause);
	}

}