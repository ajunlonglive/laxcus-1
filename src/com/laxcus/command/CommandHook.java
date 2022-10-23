/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;

/**
 * 命令钩子。<br><br>
 * 
 * 命令钩子与“异步命令调用器（EchoInvoker）或者转发命令（ShiftCommand）”配合使用，在使用时，绑定到异步命令调用器或者转发命令上。
 * 在异步命令调用器收到应答时，调用命令钩子，通知命令的发起者。<br><br>
 * 
 * 触发命令钩子的之前，也可以数据应答数据保存到命令钩子。这属于选项，具体由子类的需求决定。
 * 命令钩子与“异步命令调用器和转发命令”一样，只在当前站点使用，不会通过网络传输，不必定义串行化和其它的接口。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2012
 * @since laxcus 1.0
 */
public class CommandHook {

	/** 等待状态，默认是“真” **/
	private boolean awaiting;

	/** 执行完成产生的正常结果对象 **/
	private Object result;

	/** 执行完成产生的故障对象 **/
	private Throwable fault;

	/** 等待超时时间，默认是-1，无限制 **/
	private long timeout;
	
	/**
	 * 销毁分布资源
	 */
	protected void destroy() {
		result = null;
		fault = null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		destroy();
	}

	/**
	 * 构造命令钩子
	 */
	protected CommandHook() {
		super();
		awaiting = true;
		// 系统定义的异步调用器超时，默认是小于等于0：没有超时时间
		timeout = EchoTransfer.getInvokerTimeout();
	}

	/**
	 * 最大超时时间，对应命令中的Command.timeout
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		timeout = ms;
	}

	/**
	 * 设置基于秒时间的超时
	 * @param seconds
	 */
	public void setTimeoutWithSecond(int seconds) {
		setTimeout(seconds * 1000);
	}

	/**
	 * 返回超时时间
	 * @return 时间（长整型）
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 判断没有超时限制
	 * @return 返回真或者假
	 */
	public boolean isInfinite() {
		return timeout <= 0L;
	}
	
	/**
	 * 执行延时
	 * @param ms 超时时间，单位：毫秒
	 */
	private synchronized void delay(long ms) {
		try {
			super.wait(ms);
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	/**
	 * 唤醒延时
	 */
	private synchronized void wakeup() {
		try {
			this.notify();
		} catch (IllegalMonitorStateException e) {
			Logger.error(e);
		}
	}

	/**
	 * 判断处于等待状态
	 * @return 返回真或者假
	 */
	public boolean isAwaiting() {
		return awaiting;
	}

	/**
	 * 触发唤醒对象
	 */
	public void done() {
		awaiting = false;
		wakeup();
	}

	/**
	 * 进入等待状态，直到返回处理结果
	 */
	public void await() {
		// 设置超时触发时间
		long touchTime = -1;
		if (timeout > 0) {
			touchTime = System.currentTimeMillis() + timeout;
		}

		// 进行等待状态
		while (awaiting) {
			// 判断触发超时
			if (touchTime > 0 && System.currentTimeMillis() >= touchTime) {
				done();
				break;
			}
			delay(1000L);
		}
	}

	/**
	 * 设置执行过程中产生的故障对象，同时唤醒等待
	 * @param e 故障对象
	 */
	public void setFault(Throwable e) {
		fault = e;
		done();
	}

	/**
	 * 返回执行过程中产生的故障对象
	 * @return 故障对象
	 */
	public Throwable getFault() {
		return fault;
	}

	/**
	 * 判断有故障
	 * @return 返回真或者假
	 */
	public boolean isFault() {
		return fault != null;
	}

	/**
	 * 设置最后的处理结果对象。是Object的子类，主要是EchoProduct
	 * @param e 结果对象
	 */
	public final void setResult(Object e) {
		result = e;
		done();
	}

	/**
	 * 返回最后的处理结果对象
	 * @return 结果对象
	 */
	public Object getResult() {
		return result;
	}

}