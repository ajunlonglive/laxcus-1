/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.container;

import java.io.*;

/**
 * 没有找到应用。<br><br>
 * 
 * 没有找到应用在容器包解析中发生 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/1/2021
 * @since laxcus 1.0
 */
public class ApplicationNotFoundException extends IOException {

	private static final long serialVersionUID = 1694303819355167482L;

	/**
	 * 构造默认的没有找到应用
	 */
	public ApplicationNotFoundException() {
		super();
	}

	/**
	 * 构造没有找到应用，指定错误信息
	 * @param message 异常信息
	 */
	public ApplicationNotFoundException(String message) {
		super(message);
	}

	/**
	 * 构造没有找到应用，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ApplicationNotFoundException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造没有找到应用，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public ApplicationNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造没有找到应用，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public ApplicationNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}