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
 * 容器异常。<br><br>
 * 
 * 容器异常在容器包解析中发生 <br>
 * 
 * @author scott.liang
 * @version 1.0 06/29/2021
 * @since laxcus 1.0
 */
public class ContainerException extends IOException {

	private static final long serialVersionUID = 6938685063629949047L;

	/**
	 * 构造默认的容器异常
	 */
	public ContainerException() {
		super();
	}

	/**
	 * 构造容器异常，指定错误信息
	 * @param message 异常信息
	 */
	public ContainerException(String message) {
		super(message);
	}

	/**
	 * 构造容器异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ContainerException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造容器异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public ContainerException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造容器异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public ContainerException(String message, Throwable cause) {
		super(message, cause);
	}

}