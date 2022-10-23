/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.talk;

import java.io.*;

/**
 * 交互对话异常。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/13/2018
 * @since laxcus 1.0
 */
public class TalkException extends IOException {

	private static final long serialVersionUID = -3313330585414783242L;

	/**
	 * 构造默认的交互对话异常
	 */
	public TalkException() {
		super();
	}

	/**
	 * 构造交互对话异常，指定错误信息
	 * @param message 异常信息
	 */
	public TalkException(String message) {
		super(message);
	}

	/**
	 * 构造交互对话异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TalkException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造交互对话异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TalkException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造交互对话异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TalkException(String message, Throwable cause) {
		super(message, cause);
	}

}