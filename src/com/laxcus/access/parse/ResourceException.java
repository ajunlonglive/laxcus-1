/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

import java.io.*;

/**
 * 资源检索异常。用于“ResourceChooser”接口。
 * 
 * @author scott.liang
 * @version 1.0 12/3/2009
 * @since laxcus 1.0
 */
public class ResourceException extends IOException {

	private static final long serialVersionUID = -7399838923324205471L;

	/**
	 * 构造默认的资源检索异常
	 */
	public ResourceException() {
		super();
	}

	/**
	 * 构造资源检索异常，指定错误信息
	 * @param message 异常信息
	 */
	public ResourceException(String message) {
		super(message);
	}

	/**
	 * 构造资源检索异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ResourceException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造资源检索异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public ResourceException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造资源检索异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public ResourceException(String message, Throwable cause) {
		super(message, cause);
	}

}