/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.front.desktop.invoker.*;
import com.laxcus.front.invoker.*;
import com.laxcus.front.pool.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;

/**
 * 调用器管理池
 * 
 * @author scott.liang
 * @version 1.0 5/19/2021
 * @since laxcus 1.0
 */
public class DesktopInvokerPool extends FrontInvokerPool {

	/** 回显任务管理池 **/
	private static DesktopInvokerPool selfHandle = new DesktopInvokerPool();

	/**
	 * 构造一个默认和私有的异步调用器管理池。
	 */
	private DesktopInvokerPool() {
		super();
	}

	/**
	 * 返回异步命令管理池句柄
	 * @return
	 */
	public static DesktopInvokerPool getInstance() {
		return DesktopInvokerPool.selfHandle;
	}

	/**
	 * 启动带显示接口的调用器
	 * @param invoker 异步调用器
	 * @param display 显示接口
	 * @return 返回真或者假
	 */
	public boolean launch(EchoInvoker invoker, MeetDisplay display) {
		// 判断调用器从FrontInvoker派生
		boolean success = isFrom(invoker.getClass(), FrontInvoker.class);
		// 也允许从EchoInvoker派生，可能是定义调用器
		if (!success) {
			success = isFrom(invoker.getClass(), EchoInvoker.class);
		}
		// 启动工作
		if (success) {
			// 设置显示句柄
			if (Laxkit.isClassFrom(invoker, DesktopInvoker.class)) {
				((DesktopInvoker) invoker).setDisplay(display);
			}
			success = defaultLaunch(invoker);
		}
		return success;
	}

}