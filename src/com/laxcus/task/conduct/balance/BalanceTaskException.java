/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.balance;

import com.laxcus.task.*;

/**
 * CONDUCT.BALANCE阶段异常。<br>
 * 在执行BALANCE工作中发生。
 * 
 * @author scott.liang
 * @version 1.0 9/23/2009
 * @since laxcus 1.0
 */
public class BalanceTaskException extends TaskException {
	
	private static final long serialVersionUID = -2138794905034000678L;

	/**
	 * 构造默认的CONDUCT.BALANCE阶段异常
	 */
	public BalanceTaskException() {
		super();
	}

	/**
	 * 构造CONDUCT.BALANCE阶段异常，说明异常信息
	 * @param message 异常信息
	 */
	public BalanceTaskException(String message) {
		super(message);
	}

	/**
	 * 构造CONDUCT.BALANCE阶段异常，格式化异常信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public BalanceTaskException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 在一个异常的基础上构造CONDUCT.BALANCE阶段异常
	 * @param cause 另一个异常实例
	 */
	public BalanceTaskException(Throwable cause) {
		super(cause);
	}

	/**
	 * 在一个异常的基础上构造CONDUCT.BALANCE阶段异常，指定提示信息和异常
	 * @param message 异常信息
	 * @param cause 另一个异常实例
	 */
	public BalanceTaskException(String message, Throwable cause) {
		super(message, cause);
	}

}