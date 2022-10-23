/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub;

import java.io.*;

/**
 * TUB协议异常
 * 
 * @author scott.liang
 * @version 1.0 10/9/2020
 * @since laxcus 1.0
 */
//public class TubProtocolException extends RuntimeException {
public class TubProtocolException extends IOException {
	
	private static final long serialVersionUID = -2659728805916685625L;

	/**
	 * 构造默认的TUB协议异常
	 */
	public TubProtocolException() {
		super();
	}

	/**
	 * 构造TUB协议异常，指定错误信息
	 * @param message 异常信息
	 */
	public TubProtocolException(String message) {
		super(message);
	}

	/**
	 * 构造TUB协议异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TubProtocolException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造TUB协议异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TubProtocolException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造TUB协议异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TubProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

}