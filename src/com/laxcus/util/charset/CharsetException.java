/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.charset;

/**
 * LAXCUS字符集的编码/解码异常
 * 
 * @author scott.liang
 * @version 1.0 04/03/2009
 * @since laxcus 1.0
 */
public class CharsetException extends RuntimeException {

	private static final long serialVersionUID = -7751790818203785873L;

	/**
	 * 构造默认的LAXCUS字符集异常
	 */
	public CharsetException() {
		super();
	}

	/**
	 * 构造LAXCUS字符集异常，指定错误信息
	 * @param message 异常信息
	 */
	public CharsetException(String message) {
		super(message);
	}

	/**
	 * 构造LAXCUS字符集异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public CharsetException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造LAXCUS字符集异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public CharsetException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造LAXCUS字符集异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public CharsetException(String message, Throwable cause) {
		super(message, cause);
	}

}