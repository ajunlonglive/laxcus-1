/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

/**
 * 前端任务没有找到异常。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/23/2019
 * @since laxcus 1.0
 */
public class MissionNotFoundException extends MissionException {

	private static final long serialVersionUID = -3268804625502811617L;

	/**
	 * 构造默认的前端任务没有找到异常
	 */
	public MissionNotFoundException() {
		super();
	}

	/**
	 * 构造前端任务没有找到异常，指定错误信息
	 * @param message 异常信息
	 */
	public MissionNotFoundException(String message) {
		super(message);
	}

	/**
	 * 构造前端任务没有找到异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public MissionNotFoundException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造前端任务没有找到异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public MissionNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造前端任务没有找到异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public MissionNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}