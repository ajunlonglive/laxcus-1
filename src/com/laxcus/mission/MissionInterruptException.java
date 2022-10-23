/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

/**
 * 驱动任务中断异常
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class MissionInterruptException extends MissionException {

	private static final long serialVersionUID = 70657967590702591L;

	/**
	 * 构造默认的驱动任务中断异常
	 */
	public MissionInterruptException() {
		super();
	}

	/**
	 * 构造驱动任务中断异常，指定错误信息
	 * @param message 异常信息
	 */
	public MissionInterruptException(String message) {
		super(message);
	}

	/**
	 * 构造驱动任务中断异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public MissionInterruptException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造驱动任务中断异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public MissionInterruptException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造驱动任务中断异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public MissionInterruptException(String message, Throwable cause) {
		super(message, cause);
	}

}