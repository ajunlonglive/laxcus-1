/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * fixp exception class
 * 
 * @author scott.liang 
 * 
 * @version 1.0 3/13/2009
 * 
 * @see com.laxcus.fixp
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.io.*;

/**
 * FIXP协议异常
 * 
 * 出现在解析数据的时候
 * 
 * @author scott.liang
 * @version 1.0 2/3/2009
 * @since laxcus 1.0
 */
//public class FixpProtocolException extends RuntimeException {
public class FixpProtocolException extends IOException {

	private static final long serialVersionUID = -7754498350714564957L;

	/**
	 * 构造默认的FIXP协议异常
	 */
	public FixpProtocolException() {
		super();
	}

	/**
	 * 构造FIXP协议异常，指定错误信息
	 * @param message 异常信息
	 */
	public FixpProtocolException(String message) {
		super(message);
	}

	/**
	 * 构造FIXP协议异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public FixpProtocolException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造FIXP协议异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public FixpProtocolException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造FIXP协议异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public FixpProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

}