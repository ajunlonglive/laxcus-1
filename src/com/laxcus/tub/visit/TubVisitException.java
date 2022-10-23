/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.visit;

import java.io.*;

/**
 * 边缘应用远程访问异常。<br>
 * 这个异常发生在RPC通信过程中，是TubVisit接口实现类。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2020
 * @since laxcus 1.0
 */
public class TubVisitException extends IOException {

	private static final long serialVersionUID = -4445615976705554826L;

	/**
	 * 构造默认的边缘应用远程访问异常
	 */
	public TubVisitException() {
		super();
	}

	/**
	 * 构造边缘应用远程访问异常，指定错误信息
	 * @param message 异常信息
	 */
	public TubVisitException(String message) {
		super(message);
	}

	/**
	 * 构造边缘应用远程访问异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TubVisitException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造边缘应用远程访问异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TubVisitException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造边缘应用远程访问异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TubVisitException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 构造边缘应用远程访问异常，包括当前的错误信息和异常堆栈
	 * @param cause 另一个异常实例
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TubVisitException(Throwable cause,String format, Object... args) {
		super(String.format(format, args), cause);
	}

}