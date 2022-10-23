/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.distant;

import com.laxcus.task.*;

/**
 * SWIFT.DISTANT阶段异常。<br>
 * 在执行DISTANT工作中发生。
 * 
 * @author scott.liang
 * @version 1.0 5/3/2020
 * @since laxcus 1.0
 */
public class DistantTaskException extends TaskException {
	
	private static final long serialVersionUID = 105783029804681359L;

	/**
	 * 构造默认的SWIFT.DISTANT阶段异常
	 */
	public DistantTaskException() {
		super();
	}

	/**
	 * 构造SWIFT.DISTANT阶段异常，说明异常信息
	 * @param message 异常信息
	 */
	public DistantTaskException(String message) {
		super(message);
	}

	/**
	 * 构造SWIFT.DISTANT阶段异常，格式化异常信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public DistantTaskException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 在一个异常的基础上构造SWIFT.DISTANT阶段异常
	 * @param cause 另一个异常实例
	 */
	public DistantTaskException(Throwable cause) {
		super(cause);
	}

	/**
	 * 在一个异常的基础上构造SWIFT.DISTANT阶段异常，指定提示信息和异常
	 * @param message 异常信息
	 * @param cause 另一个异常实例
	 */
	public DistantTaskException(String message, Throwable cause) {
		super(message, cause);
	}

}