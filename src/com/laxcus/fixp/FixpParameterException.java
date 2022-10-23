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

/**
 * FIXP参数异常
 * 
 * 出现在生成数据的时候。
 * 
 * @author scott.liang
 * @version 1.0 2/3/2009
 * @since laxcus 1.0
 */
public class FixpParameterException extends RuntimeException {

	private static final long serialVersionUID = -7754498350714564957L;

	/**
	 * 构造默认的FIXP参数异常
	 */
	public FixpParameterException() {
		super();
	}

	/**
	 * 构造FIXP参数异常，指定错误信息
	 * @param message 异常信息
	 */
	public FixpParameterException(String message) {
		super(message);
	}

	/**
	 * 构造FIXP参数异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public FixpParameterException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造FIXP参数异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public FixpParameterException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造FIXP参数异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public FixpParameterException(String message, Throwable cause) {
		super(message, cause);
	}

}