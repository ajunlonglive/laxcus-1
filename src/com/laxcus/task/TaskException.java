/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import java.io.*;

/**
 * 分布任务异常。<br><br>
 * 
 * 分布任务异常在分布任务处理的各个环节中产生，由分布数据计算和分布数据构建引发，它是CONDUCT和ESTABLISH命令的基础异常。
 * <br>
 * 
 * @author scott.liang
 * @version 1.0 02/02/2009
 * @since laxcus 1.0
 */
public class TaskException extends IOException {

	private static final long serialVersionUID = 6938685063629949047L;

	/**
	 * 构造默认的分布任务异常
	 */
	public TaskException() {
		super();
	}

	/**
	 * 构造分布任务异常，指定错误信息
	 * @param message 异常信息
	 */
	public TaskException(String message) {
		super(message);
	}

	/**
	 * 构造分布任务异常，格式化错误信息
	 * @param format 格式化语句
	 * @param args 参数
	 */
	public TaskException(String format, Object... args) {
		super(String.format(format, args));
	}

	/**
	 * 构造分布任务异常，包容另一个异常
	 * @param cause 另一个异常实例
	 */
	public TaskException(Throwable cause) {
		super(cause);
	}

	/**
	 * 构造分布任务异常，包括当前的错误信息和异常堆栈
	 * @param message 异常消息
	 * @param cause 另一个异常实例
	 */
	public TaskException(String message, Throwable cause) {
		super(message, cause);
	}

}