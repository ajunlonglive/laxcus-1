/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.pool;

import com.laxcus.front.pool.*;

/**
 * 基于交互模式的异步调用器管理池，被终端和控制台使用。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2012
 * @since laxcus 1.0
 */
public class MeetInvokerPool extends FrontInvokerPool {

	/** 回显任务管理池 **/
	private static MeetInvokerPool selfHandle = new MeetInvokerPool();

	/**
	 * 构造一个默认和私有的异步调用器管理池。
	 */
	private MeetInvokerPool() {
		super();
	}

	/**
	 * 返回异步命令管理池句柄
	 * @return
	 */
	public static MeetInvokerPool getInstance() {
		return MeetInvokerPool.selfHandle;
	}
}