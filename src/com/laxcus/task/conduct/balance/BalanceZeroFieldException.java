/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.balance;

/**
 * BALANCE阶段的零分区异常。<br>
 * 执行BALANCE工作中发生，原因是上级的FROM/TO阶段没有产生分区。
 * 
 * @author scott.liang
 * @version 1.0 9/23/2009
 * @since laxcus 1.0
 */
public class BalanceZeroFieldException extends BalanceTaskException {
	
	private static final long serialVersionUID = -2138794905034000678L;

	/**
	 * 构造默认的BALANCE阶段的零分区异常
	 */
	public BalanceZeroFieldException() {
		super();
	}

	/**
	 * 构造BALANCE阶段的零分区异常，说明异常信息
	 * @param message 异常信息
	 */
	public BalanceZeroFieldException(String message) {
		super(message);
	}

	/**
	 * 构造BALANCE阶段的零分区异常，格式化异常信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public BalanceZeroFieldException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 在一个异常的基础上构造BALANCE阶段的零分区异常
	 * @param cause 另一个异常实例
	 */
	public BalanceZeroFieldException(Throwable cause) {
		super(cause);
	}

	/**
	 * 在一个异常的基础上构造BALANCE阶段的零分区异常，指定提示信息和异常
	 * @param message 异常信息
	 * @param cause 另一个异常实例
	 */
	public BalanceZeroFieldException(String message, Throwable cause) {
		super(message, cause);
	}

}