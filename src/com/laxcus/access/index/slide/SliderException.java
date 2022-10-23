/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

import java.io.*;

/**
 * 对象定位计算异常。<br>
 * 在执行“对象定位计算”时发生的异常。
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class SliderException extends IOException {

	private static final long serialVersionUID = 6938685063629949047L;

	/**
	 * 构造默认的对象定位计算异常
	 */
	public SliderException() {
		super();
	}

	/**
	 * 构造对象定位计算异常，指定错误信息
	 * @param message 异常信息
	 */
	public SliderException(String message) {
		super(message);
	}

	/**
	 * 构造对象定位计算异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public SliderException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造对象定位计算异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public SliderException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造对象定位计算异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public SliderException(String message, Throwable cause) {
		super(message, cause);
	}

}