/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access;

import java.io.*;

/**
 * JNI接口访问异常。<br><br>
 * 
 * 在调用底层JNI接口前，进行参数检查时发生的错误。<br>
 * 
 * @author scott.liang
 * @version 1.0 12/12/2020
 * @since laxcus 1.0
 */
public class AccessException extends IOException {

	private static final long serialVersionUID = -4401913775585019418L;

	/**
	 * 构造默认的JNI接口访问异常
	 */
	public AccessException() {
		super();
	}

	/**
	 * 构造JNI接口访问异常，指定错误信息
	 * @param message 异常信息
	 */
	public AccessException(String message) {
		super(message);
	}

	/**
	 * 构造JNI接口访问异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public AccessException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造JNI接口访问异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public AccessException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造JNI接口访问异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public AccessException(String message, Throwable cause) {
		super(message, cause);
	}

}