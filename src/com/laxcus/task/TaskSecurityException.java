/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

/**
 * 分布组件安全异常。<br><br>
 * 
 * 这个异常通常在签名校验不匹配时产生，即数据操作人与实际的数据持有人不一致。
 * 
 * @author scott.liang
 * @version 1.0 07/12/2009
 * @since laxcus 1.0
 */
public class TaskSecurityException extends TaskException {

	private static final long serialVersionUID = 3953675364773857007L;

	/**
	 * 构造默认的分布组件安全异常
	 */
	public TaskSecurityException() {
		super();
	}

	/**
	 * 构造分布组件安全异常，指定错误信息
	 * @param message 异常信息
	 */
	public TaskSecurityException(String message) {
		super(message);
	}

	/**
	 * 构造分布组件安全异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TaskSecurityException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造分布组件安全异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TaskSecurityException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造分布组件安全异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TaskSecurityException(String message, Throwable cause) {
		super(message, cause);
	}

}