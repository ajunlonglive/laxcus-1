/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.fork;

import com.laxcus.task.*;

/**
 * CONTACT.FORK阶段异常。<br>
 * 在执行FORK工作中发生。
 * 
 * @author scott.liang
 * @version 1.0 5/8/2020
 * @since laxcus 1.0
 */
public class ForkTaskException extends TaskException {
	
	private static final long serialVersionUID = 5159133742055686941L;

	/**
	 * 构造默认的CONTACT.FORK阶段异常
	 */
	public ForkTaskException() {
		super();
	}

	/**
	 * 构造CONTACT.FORK阶段异常，说明异常信息
	 * @param message 异常信息
	 */
	public ForkTaskException(String message) {
		super(message);
	}

	/**
	 * 构造CONTACT.FORK阶段异常，格式化异常信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public ForkTaskException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 在一个异常的基础上构造CONTACT.FORK阶段异常
	 * @param cause 另一个异常实例
	 */
	public ForkTaskException(Throwable cause) {
		super(cause);
	}

	/**
	 * 在一个异常的基础上构造CONTACT.FORK阶段异常，指定提示信息和异常
	 * @param message 异常信息
	 * @param cause 另一个异常实例
	 */
	public ForkTaskException(String message, Throwable cause) {
		super(message, cause);
	}

}